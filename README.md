# akka-persistence-cassandra-benchmark

Benchmarks of akka persistence cassandra project (https://github.com/akka/akka-persistence-cassandra).

Specifically Replay, Strong and Weak Scaling (https://www.sharcnet.ca/help/index.php/Measuring_Parallel_Scaling_Performance) and other benchmarks.

Allows tuning of replay dispatchers, threadpool, partition size, buffer size etc.

Originally used to compare performance improvement between versions "0.7-SNAPSHOT" with the blocking iterator abstraction and version "0.15-SNAPSHOT" using Akka Persistence Query EventsByPersistenceId stream for asynchronous non blocking replay.
