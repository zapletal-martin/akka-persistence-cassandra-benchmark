package examples

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object ExampleApp extends App {

  implicit val system = ActorSystem("ExampleApp")
  implicit val materializer = ActorMaterializer()

  PrepareData.prepare(100, 100, 5000)

  /*val result = Await.result(EventsByTag.eventsByTag(), 30.seconds)
  println("========================================")
  println(result)
  println("========================================")*/

  val result = EventsByPersistenceId.eventsByPersistenceId("0")

  result.onComplete { _ =>
    materializer.shutdown()
    Await.result(system.terminate(), 10.seconds)
  }
}
