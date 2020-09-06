package primenumbers

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.ThriftMux
import com.twitter.server.TwitterServer
import com.twitter.finagle._
import com.twitter.finagle.http.service.HttpResponseClassifier
import com.twitter.finagle.thriftmux.service.ThriftMuxResponseClassifier
import com.twitter.util.{Await, Duration}
import io.finch._
import io.finch.circe._
import io.circe.generic.auto._
import io.finch.syntax.get
import primenumbers.thrift.PrimeNumbersService


object PrimeNumbersApi extends TwitterServer {

  private val port = flag[Int]("port", 8081, "Port for the proxy service server")
  private val thriftClientRequestTimeout = Duration.fromSeconds(10)

  val client: PrimeNumbersService.MethodPerEndpoint = ThriftMux.client
    .withRequestTimeout(thriftClientRequestTimeout)
    .withResponseClassifier(ThriftMuxResponseClassifier.ThriftExceptionsAsFailures)
    .build[PrimeNumbersService.MethodPerEndpoint](":8082", "proxy-service-client")


  val api: Endpoint[AsyncStream[Int]] = get("prime" :: path[Int].shouldNot(beLessThan(2))) { n: Int =>
    client.getPrimeNumbers(n).map { results => Ok(AsyncStream.fromSeq(results)) }
      .onFailure(e => println(s"An error occurred while calling the prime numbers server: ${e.getMessage}"))
  }.handle {
    case e: RequestTimeoutException => RequestTimeout(e)
  }

  lazy val server: ListeningServer = Http.server
    .withLabel("proxy-service-server")
    .withResponseClassifier(HttpResponseClassifier.ServerErrorsAsFailures)
    .withStreaming(true)
    .serve(s":${port()}", service = api.toService)

  def main(): Unit = {
    closeOnExit(server)
    Await.ready(server)
  }
}
