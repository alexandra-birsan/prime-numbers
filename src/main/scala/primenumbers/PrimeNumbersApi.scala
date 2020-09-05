package primenumbers

import com.twitter.finagle.ThriftMux
import com.twitter.server.TwitterServer
import primenumbers.thrift.PrimeNumbersService
import com.twitter.finagle._
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.thriftmux.service.ThriftMuxResponseClassifier
import com.twitter.util.Await
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.finch.syntax.get

object PrimeNumbersApi extends TwitterServer {

  private val port = flag[Int]("port", 8081, "Port for the proxy service server")

  def main: Unit = {

    val client = ThriftMux.client
      .withResponseClassifier(ThriftMuxResponseClassifier.ThriftExceptionsAsFailures)
      .build[PrimeNumbersService.MethodPerEndpoint](":8082", "proxy-service-client")


    case class Results(primes: List[Int])

    val api: Endpoint[List[Int]] = get("prime") {
      client.getPrimeNumbers(10).map { results => Ok(results.toList) }
    }

    val server = Http.server
      .withLabel("proxy-service-server")
      .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
      .serve(s":${port()}", service = api.toService)

    closeOnExit(server)
    Await.ready(server)

  }
}
