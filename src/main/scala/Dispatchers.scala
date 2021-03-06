trait Dispatchers {

  def fixedThreadPool(
      fixedPoolSize: Int,
      throughput: Int): String =
  s"""my-dispatcher {
    |  type = "Dispatcher"
    |  executor = "thread-pool-executor"
    |
    |  thread-pool-executor {
    |    fixed-pool-size = $fixedPoolSize
    |  }
    |
    |  throughput = $throughput
    |}"""

  def forkJoin(
      parallelismMin: Int,
      parallelismMax: Int,
      parallelismFactor: Double,
      throughput: Int): String =
    s"""
      |my-dispatcher {
      |  type = "Dispatcher"
      |  executor = "fork-join-executor"
      |
      |  fork-join-executor {
      |    parallelism-min = $parallelismMin
      |    parallelism-max = $parallelismMax
      |    parallelism-factor = $parallelismFactor
      |  }
      |
      |  throughput = $throughput
      |}"""
}
