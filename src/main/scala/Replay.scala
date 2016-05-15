import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Try

import akka.pattern.ask
import Observer.{Results, GetResults}
import akka.actor.ActorSystem
import akka.util.Timeout
import com.typesafe.config.Config

object Replay {

  def replay(
    numberOfActors: Int,
    threads: Option[Int] = None
  )(config: Config): Results = {
    val dispatcher = Try(config.getString("cassandra-journal.plugin-dispatcher")).toOption

    println(s"replay $numberOfActors actors with $dispatcher dispatcher using $threads threads")

    implicit val system = ActorSystem("recover", config)
    implicit val timeout = Timeout(5.minutes)

    val initializer = system.actorOf(TimerActor.props(0, None, Array.empty), "0")
    Thread.sleep(3000)
    system.stop(initializer)
    Thread.sleep(1000)

    val observer = system.actorOf(Observer.props(numberOfActors), "observer")

    (0 until numberOfActors)
      .par
      .map(i => system.actorOf(TimerActor.props(i, Some(observer), Array.empty), i.toString))

    val results = Await.result(
      (observer ? GetResults).mapTo[Results],
      5.minutes)

    Await.result(system.terminate(), Duration.Inf)

    results
  }
}
