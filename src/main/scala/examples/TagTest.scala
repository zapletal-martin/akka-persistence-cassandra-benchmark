package examples

import scala.concurrent.Await
import scala.concurrent.duration._

import akka.pattern.ask
import akka.actor.{ActorSystem, Props}
import akka.persistence.PersistentActor
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.journal.Tagged
import akka.persistence.query.PersistenceQuery
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory

object TagTest extends App {

  val partitionSize = 50
  val resultSize = 25

  val storeDispatcher =
    s"""
       |cassandra-journal {
       |  max-result-size = $resultSize
       |  max-result-size-replay = $resultSize
       |  target-partition-size = $partitionSize
       |  max-message-batch-size = $partitionSize
       |}
       |
       |cassandra-query-journal {
       |  delayed-event-timeout = 30s
       |  max-buffer-size = $resultSize
       |  max-result-size-query = $resultSize
       |}
    """.stripMargin

  val storeConfig = ConfigFactory.parseString(storeDispatcher)
    .withFallback(ConfigFactory.load())

  class TagActor(override val persistenceId: String) extends PersistentActor {

    override def receiveRecover: Receive = {
      case _ =>
    }

    override def receiveCommand: Receive = {
      case message =>
        persistAsync(message) { _ =>
          sender() ! "got it"
        }
    }
  }

  implicit val timeout = Timeout(3.second)

  implicit val system = ActorSystem("TagTest", storeConfig)

  val actor = system.actorOf(Props(new TagActor("0")))

  Await.result(actor ? "0", timeout.duration)
  Await.result(actor ? Tagged("1", Set("tag1")), timeout.duration)
  Await.result(actor ? "2", timeout.duration)
  Await.result(actor ? Tagged("3", Set("tag1")), timeout.duration)

  implicit val materializer = ActorMaterializer()

  lazy val queries: CassandraReadJournal =
    PersistenceQuery(system).readJournalFor[CassandraReadJournal]("cassandra-query-journal")

  queries
    .eventsByTag("tag1", 0)
    .runForeach(println)
}
