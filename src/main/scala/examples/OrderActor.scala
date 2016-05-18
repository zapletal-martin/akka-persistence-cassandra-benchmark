package examples

import scala.concurrent.duration.Duration

import akka.persistence.PersistentActor
import OrderActor._
import akka.persistence.journal.Tagged
import org.scalactic.{Good, Or}

object OrderActor {

  sealed trait Command
  case class UpdateBalanceCommand(value: Double) extends Command
  case class BalanceUpdatedEvent(value: Double) extends Event

  sealed trait Event
  case class UpdateGroupBalanceCommand(value: Double) extends Command
  case class GroupBalanceUpdatedEvent(value: Double) extends Event

  case class State(balance: Double) {
    def update(update: BalanceUpdatedEvent) = {
      State(balance + update.value)
    }
  }

  def extractId(name: String): String = name

  val initialState = State(0)

  implicit class ValidateUpdateBalanceCommand(command: UpdateBalanceCommand) {
    def validate(): Or[BalanceUpdatedEvent, String] = {
      Good(BalanceUpdatedEvent(command.value))
    }
  }

  implicit class ValidateUpdateGroupBalanceCommand(command: UpdateGroupBalanceCommand) {
    def validate(): Or[GroupBalanceUpdatedEvent, String] = {
      Good(GroupBalanceUpdatedEvent(command.value))
    }
  }

  def processValidationErrors(errors: String): Unit = {
    println(errors)
  }
}

class OrderActor(protected[this] val passivationTimeout: Duration) extends PersistentActor {

  override val persistenceId: String = extractId(self.path.name)
  override def receiveCommand: Receive = active(initialState)

  private def active(
    balance: State
  ): Receive = {
    case command: Command => command match {
      case cmd: UpdateBalanceCommand =>
        cmd.validate().fold({ balanceUpdated =>
          persist(balanceUpdated) { persisted =>
            val updatedState = balance.update(persisted)
            sender() ! updatedState
            context.become(active(updatedState))
          }
        },
        processValidationErrors)

      case cmd: UpdateGroupBalanceCommand =>
        cmd.validate().fold({ groupBalanceUpdated =>
          persist(Tagged(groupBalanceUpdated, Set("tag1"))) { persisted =>
            sender() ! groupBalanceUpdated
          }
        },
        processValidationErrors)
    }
  }

  override def receiveRecover: Receive = {
    var state: State = initialState

    {
      case balanceUpdated: BalanceUpdatedEvent =>
        state = state.update(balanceUpdated)
    }
  }
}

