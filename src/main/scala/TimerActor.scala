import TimerActor.{PersistedEvent, HowMuch, Event}
import Observer.ImDone
import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{RecoveryCompleted, PersistentActor}

object TimerActor {
  final case class Event(i: Int) extends AnyVal
  final case class PersistedEvent(i: Int, event: Array[Byte])
  case object HowMuch

  def props(
      persistenceId: Int,
      observer: Option[ActorRef],
    event: Array[Byte]) =
    Props(new TimerActor(persistenceId.toString, observer, event))
}

class TimerActor(
    override val persistenceId: String,
    observer: Option[ActorRef],
    event: Array[Byte])
  extends PersistentActor
  with ActorLogging {
  var first = true
  val t0 = System.nanoTime()
  var t1 = System.nanoTime()
  var count = 0

  // log.info(s"Constructor ${System.currentTimeMillis()}")

  override def receiveRecover: Receive = {
    case event: PersistedEvent =>
      if(first) {
        // log.info(s"First evt ${System.currentTimeMillis()}")
        first = false
        t1 = System.nanoTime()
      }

    case RecoveryCompleted =>
      if(!first) {
        // log.info(s"RecoveryCompleted ${System.currentTimeMillis()}")
        val durationSinceConstructor = (System.nanoTime() - t0) / 1000 / 1000
        val durationSinceFirstEvent = (System.nanoTime() - t1) / 1000 / 1000
        // log.info(s"recovery completed since constructor in $durationSinceConstructor ms, since first event $durationSinceFirstEvent ms for name ${self.path.name}")
        observer.foreach(_ ! ImDone(persistenceId, durationSinceConstructor, durationSinceFirstEvent))
      }
  }

  override def receiveCommand: Receive = {
    case evt: Event => persistAsync(PersistedEvent(evt.i, event)) { _ =>
      count = count + 1
    }

    case HowMuch =>
      sender() ! count
  }
}
