package examples

import scala.concurrent.duration._

import akka.actor.{ActorSystem, Props}
import examples.OrderActor.{UpdateBalanceCommand, UpdateGroupBalanceCommand}

object PrepareData {

  def prepare(actors: Int, events: Int, nthGrouped: Int)(implicit system: ActorSystem) = {
    val r = scala.util.Random
    var counter = 0

    for(i <- 0 until actors) {

      val actor = system.actorOf(Props(new OrderActor(10.seconds)), i.toString)

      for (j <- 0 until events) {
        val rand = r.nextInt(10000)

        if (counter % nthGrouped == 0) {
          actor ! UpdateGroupBalanceCommand(rand)
        } else {
          actor ! UpdateBalanceCommand(rand)
        }

        counter = counter + 1
      }

      Thread.sleep(100)
    }
  }
}
