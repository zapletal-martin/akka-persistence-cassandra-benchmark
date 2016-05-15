import Observer.Results

object ResultProcessor {

  def processResults(resultsList: List[Results]) = {
    println("=====================================================================")
    println("|                          FINAL RESULTS                            |")
    resultsList.foreach(processResult)
    println("|                                                                   |")
    println("=====================================================================")
  }

  def processResult(results: Results) = {
    println("=====================================================================")
    println(s"Total recovery completed in ${results.total} ms, count ${results.numberOfActors}")
    println(s"Max since constructor ${results.maxSinceConstructor._1} -> ${results.maxSinceConstructor._2} ms")
    println(s"Min since constructor ${results.minSinceConstructor._1} -> ${results.minSinceConstructor._2} ms")

    println(s"Max since first event ${results.maxSinceFirstEvent._1} -> ${results.maxSinceFirstEvent._2} ms")
    println(s"Min since first event ${results.minSinceFirstEvent._1} -> ${results.minSinceFirstEvent._2} ms")

    println(s"Avg since constructor ${results.avgSinceConstructor} ms")
    println(s"Avg since first event ${results.avgSinceFirstEvent} ms")
    println("=====================================================================")
  }
}
