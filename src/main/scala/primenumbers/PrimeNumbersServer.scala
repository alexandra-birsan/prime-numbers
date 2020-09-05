package primenumbers

import com.twitter.finagle.ThriftMux
import com.twitter.finagle.thrift.Protocols
import com.twitter.finagle.thriftmux.service.ThriftMuxResponseClassifier
import com.twitter.server.TwitterServer
import com.twitter.util.{Await, Future}
import primenumbers.thrift.PrimeNumbersService

object PrimeNumbersServer extends TwitterServer {

  private val port = flag[Int]("port", 8082, "Port for the prime number server")

  def main(): Unit = {

    val service: PrimeNumbersService.MethodPerEndpoint = (n: Int) => Future.value(List(2, 3, 5, 7))

    val finagledService: PrimeNumbersService.FinagledService = new PrimeNumbersService.FinagledService(service, Protocols.binaryFactory())

    val server = ThriftMux.server
      .withResponseClassifier(ThriftMuxResponseClassifier.ThriftExceptionsAsFailures)
      .withLabel("prime-number-server")
      .serve(s":${port()}", finagledService)

    closeOnExit(server)
    Await.ready(server)
  }
}