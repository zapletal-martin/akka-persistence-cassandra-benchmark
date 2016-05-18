package examples

import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import examples.OrderActor.GroupBalanceUpdatedEvent

object EventsByTag {

  def eventsByTag()(implicit system: ActorSystem, materializer: Materializer): Future[Double] = {

    lazy val queries: CassandraReadJournal =
      PersistenceQuery(system).readJournalFor[CassandraReadJournal]("cassandra-query-journal")

    val currentGroupBalance = queries
      .currentEventsByTag("tag1", 0)
      .collect {
        case EventEnvelope(_, _, _, GroupBalanceUpdatedEvent(value)) => value
      }
      .runFold(0d)(_ + _)

    currentGroupBalance
  }
}
