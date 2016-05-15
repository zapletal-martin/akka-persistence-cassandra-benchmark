import scala.collection.mutable

import Observer.{GetResults, ImDone, Results}
import akka.actor.{Actor, ActorLogging, ActorRef, Props}

object Observer {
  final case class ImDone(from: String, durationSinceConstructor: Long, durationSinceFirstEvent: Long)

  case object GetResults
  final case class Results(
    numberOfActors: Int,
    total: Long,
    maxSinceConstructor: (String, Long),
    minSinceConstructor: (String, Long),
    maxSinceFirstEvent: (String, Long),
    minSinceFirstEvent: (String, Long),
    avgSinceConstructor: Long,
    avgSinceFirstEvent: Long)

  def props(max: Int): Props = Props(new Observer(max))
}

class Observer(max: Int) extends Actor with ActorLogging {
  val results: collection.mutable.Map[String, (Long, Long)] = mutable.Map[String, (Long, Long)]()
  var count = 0
  var t0 = System.nanoTime()
  var resultRecipient: ActorRef = _

  override def receive: Receive = {
    case GetResults =>
      resultRecipient = sender()

    case ImDone(from, sinceConstructor, sinceFirstEvent) =>
      count = count + 1
      results += from -> (sinceConstructor -> sinceFirstEvent)

      if (count == max) {
        val duration = (System.nanoTime() - t0) / 1000 / 1000
        val maxSinceConstructor = results.maxBy(_._2._1)
        val minSinceConstructor = results.minBy(_._2._1)

        val maxSinceFirstEvent = results.maxBy(_._2._2)
        val minSinceFirstEvent = results.minBy(_._2._2)

        val avgSinceConstructor = results.values.map(_._1).sum / results.size
        val avgSinceFirstEvent = results.values.map(_._2).sum / results.size

        resultRecipient ! Results(
          max,
          duration,
          maxSinceConstructor._1 -> maxSinceConstructor._2._1,
          minSinceConstructor._1 -> minSinceConstructor._2._1,
          maxSinceFirstEvent._1 -> maxSinceFirstEvent._2._2,
          minSinceFirstEvent._1 -> minSinceFirstEvent._2._2,
          avgSinceConstructor,
          avgSinceFirstEvent)
      }
  }
}
