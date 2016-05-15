import scala.concurrent.Await

import akka.pattern.ask
import scala.concurrent.duration._

import TimerActor.{HowMuch, Event}
import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.Config

object Store {

  def store(
      event: Array[Byte],
      numberOfActors: Int,
      waitAfterActor: Option[Long],
      numberOfEventsPerActor: Int,
      waitAfterEvent: Option[Long]
    )(config: Config): Unit = {

    println(s"store $numberOfActors actors, each with $numberOfEventsPerActor events, event size ${event.length} bytes")

    implicit val system = ActorSystem("store", config)
    implicit val timeout = Timeout(100.second)

    for(i <- 0 until numberOfActors) {
      val a = system.actorOf(TimerActor.props(i, None, event), i.toString)

      for(j <- 0 until numberOfEventsPerActor) {
        a ! Event(j)
        waitAfterEvent.foreach(x => Thread.sleep(x))
      }

      waitAfterActor.foreach(x => Thread.sleep(x))
      Await.result(a ? HowMuch, timeout.duration)
    }

    Await.result(system.terminate(), Duration.Inf)
    println("store done")
  }
}
