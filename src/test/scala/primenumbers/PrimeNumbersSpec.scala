package primenumbers

import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{ListeningServer, Service}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.finch.circe._
import io.finch.test.ServiceIntegrationSuite
import org.scalatest.{BeforeAndAfterAll, fixture}
import primenumbers.PrimeNumbersSpec.extractNumberListFromResponse

class PrimeNumbersSpec extends fixture.FlatSpec with ServiceIntegrationSuite with BeforeAndAfterAll {

  override def createService(): Service[Request, Response] = PrimeNumbersApi.api.toService

  private val primeNumbersServer: ListeningServer = PrimeNumbersServer.server

  "Get prime numbers" should "return a Bad Request response when the parameter is negative" in { service =>
    val response: Response = service(Request("/prime/-5"))
    assertResult(Status.BadRequest)(response.status)
  }

  it should "return a Bad Request response when the parameter is less than 2" in { service =>
    val response: Response = service(Request("/prime/1"))
    assertResult(Status.BadRequest)(response.status)
  }

  it should "return 2 when the parameter is 2" in { service =>
    val response: Response = service(Request("/prime/2"))
    assertResult(Status.Ok)(response.status)
    assertResult(List(2))(extractNumberListFromResponse(response))
  }


  it should "return all the prime numbers up to 17" in { service =>
    val response: Response = service(Request("/prime/17"))
    assertResult(Status.Ok)(response.status)
    assertResult(List(2, 3, 5, 7, 11, 13, 17))(extractNumberListFromResponse(response))
  }

  override def afterAll() {
    Await.ready(primeNumbersServer.close())
  }

}

object PrimeNumbersSpec {

  def extractNumberListFromResponse(response: Response): List[Int] = {
    response.contentString.split("\\n").map(_.toInt).toList
  }
}