import com.typesafe.config.ConfigFactory

object IncreasingNrActorsSingleEvtLarge extends App with Dispatchers {

  val dispatcher =
    s"""
      |${fixedThreadPool(10)}
      |
      |cassandra-journal {
      |  plugin-dispatcher = "my-dispatcher"
      |  replay-dispatcher = "my-dispatcher"
      |  max-result-size = 50001
      |  max-result-size-replay = 50001
      |  target-partition-size = 500000
      |}
      |
      |cassandra-query-journal {
      |  plugin-dispatcher = "my-dispatcher"
      |  max-buffer-size = 50001
      |  max-result-size-query = 50001
      |}
    """.stripMargin

  val config = ConfigFactory.parseString(dispatcher)
    .withFallback(ConfigFactory.load())

  Store.store(Array.fill(1000000)("1".toByte), 10000, Some(10), 1, None)(config)

  val results = List(1, 10, 100, 1000, 10000).map { i =>
    val results = Replay.replay(i)(config)
    ResultProcessor.processResult(results)
    results
  }

  ResultProcessor.processResults(results)
}
