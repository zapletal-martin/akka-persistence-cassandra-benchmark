package examples

import akka.actor.ActorSystem
import akka.persistence.cassandra.query.scaladsl.CassandraReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}

object AllPersistenceIds {

  def allPersistenceIds()(implicit system: ActorSystem, materializer: Materializer) = {
    // Source ~> transform? ~> KafkaSink

    def kafkaSink() = {
      Sink.foreach(println)
    }

    val transform = Flow[String].map(_.toUpperCase)


    lazy val queries: CassandraReadJournal =
      PersistenceQuery(system).readJournalFor[CassandraReadJournal]("cassandra-query-journal")

    import akka.stream.scaladsl.GraphDSL.Implicits._

    queries.allPersistenceIds() ~> transform ~> kafkaSink
  }
}
