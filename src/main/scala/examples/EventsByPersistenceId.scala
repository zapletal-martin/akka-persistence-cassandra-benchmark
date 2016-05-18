package examples

import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.{EventEnvelope, PersistenceQuery}
import akka.stream.Materializer
import examples.OrderActor.BalanceUpdatedEvent
import org.apache.commons.collections4.queue.CircularFifoQueue

object EventsByPersistenceId {

  def eventsByPersistenceId(
      persistenceId: String
    )(implicit system: ActorSystem, materializer: Materializer) = {

    lazy val queries: CassandraReadJournal =
      PersistenceQuery(system).readJournalFor[CassandraReadJournal]("cassandra-query-journal")

    queries
      .eventsByPersistenceId(persistenceId, 0, Long.MaxValue)
      .collect {
        case EventEnvelope(_, _, _, BalanceUpdatedEvent(value)) => value
      }
      .scan(new CircularFifoQueue[Double](5)){ (s, d) => s.add(d); s }
      .runForeach(println)
  }
}
