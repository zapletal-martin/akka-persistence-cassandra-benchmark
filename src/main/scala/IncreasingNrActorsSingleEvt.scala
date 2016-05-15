import com.typesafe.config.ConfigFactory

object IncreasingNrActorsSingleEvt extends App with Dispatchers {

  val partitionSize = 50
  val resultSize = 25

  val dispatcher =
    s"""
      |${fixedThreadPool(64)}
      |
      |cassandra-journal {
      |  plugin-dispatcher = "my-dispatcher"
      |  replay-dispatcher = "my-dispatcher"
      |  max-result-size = $resultSize
      |  max-result-size-replay = $resultSize
      |  target-partition-size = $partitionSize
      |  max-message-batch-size = $partitionSize
      |}
      |
      |cassandra-query-journal {
      |  plugin-dispatcher = "my-dispatcher"
      |  max-buffer-size = $resultSize
      |  max-result-size-query = $resultSize
      |}
      |
      |akka.persistence {
      |  journal-plugin-fallback {
      |    circuit-breaker {
      |      max-failures = 10
      |      call-timeout = 1000s
      |      reset-timeout = 30s
      |    }
      |  }
      |}
    """.stripMargin

  val config = ConfigFactory.parseString(dispatcher)
    .withFallback(ConfigFactory.load())

  // Store.store(Array.empty, 25000, Some(10), 100, None)(config)

  val results = List(1, 10, 100, 500, 1000, 2500, 7500, 10000, 25000).map { i =>
    val result = Replay.replay(i)(config)
    ResultProcessor.processResult(result)
    result
  }

  ResultProcessor.processResults(results)
}
