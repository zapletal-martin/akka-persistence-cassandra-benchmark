import com.typesafe.config.ConfigFactory

object WeakScaling extends App with Dispatchers {

  val partitionSize = 50
  val resultSize = 25

  val storeDispatcher =
    s"""
       |${fixedThreadPool(10)}
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

  val results = List(1, 2, 4, 6, 8, 12, 16, 24, 32, 40, 64).map { i =>
    val dispatcher =
      s"""
         |${fixedThreadPool(i)}
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

    val result = Replay.replay(64 * i, Some(i))(replayConfig)
    ResultProcessor.processResult(result)
    result
  }

  ResultProcessor.processResults(results)
}
