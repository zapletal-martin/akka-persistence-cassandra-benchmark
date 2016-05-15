import com.typesafe.config.ConfigFactory

object StrongScaling extends App with Dispatchers {

  val partitionSize = 50
  val resultSize = 25

  val storeDispatcher =
    s"""
       |${forkJoin(2, 8, 2.0, 100)}
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
    """.stripMargin

  val storeConfig = ConfigFactory.parseString(storeDispatcher)
    .withFallback(ConfigFactory.load())

  // Store.store(Array.fill(100)("1".toByte), 10000, Some(50), 1000, None)(storeConfig)

  val results = List(1, 2, 4, 6, 8, 12, 16, 24, 32, 40, 64, 128).map { i =>
    val dispatcher =
      s"""
         |${forkJoin(1, i, 10, 128)}
         |
         |cassandra-journal {
         |  plugin-dispatcher = "my-dispatcher"
         |  replay-dispatcher = "my-dispatcher"
         |  max-result-size = $resultSize
         |  max-result-size-replay = $resultSize
         |  target-partition-size = $partitionSize
         |}
         |
         |cassandra-query-journal {
         |  plugin-dispatcher = "my-dispatcher"
         |  max-buffer-size = $resultSize
         |  max-result-size-query = $resultSize
         |}
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

    val replayConfig = ConfigFactory.parseString(dispatcher)
      .withFallback(ConfigFactory.load())

    val results = Replay.replay(1000, Some(i))(replayConfig)
    ResultProcessor.processResult(results)
    results
  }

  ResultProcessor.processResults(results)
}
