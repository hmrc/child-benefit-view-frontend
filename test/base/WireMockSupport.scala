package base

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.scalatest.{Assertion, BeforeAndAfterAll, BeforeAndAfterEach}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

trait WireMockSupport extends BaseSpec with BeforeAndAfterEach with BeforeAndAfterAll {

  private val defaultSeconds = 100;
  implicit val defaultTimeout: FiniteDuration = FiniteDuration(defaultSeconds, TimeUnit.SECONDS)

  val wiremockPort = 11111
  val defaultWireMockConfig = Map(
    "microservice.services.auth.port"                      -> wiremockPort,
    "microservice.services.child-benefit-entitlement.port" -> wiremockPort
  )

  private val stubHost = "localhost"

  protected val wiremockBaseUrl: String = s"http://localhost:$wiremockPort"
  private val wireMockServer = new WireMockServer(wireMockConfig().port(wiremockPort))

  def afterReset(block: => Assertion): Assertion = {
    WireMock.reset()
    block
  }

  override protected def beforeAll() = {
    super.beforeAll()
    wireMockServer.stop()
    wireMockServer.start()
    WireMock.configureFor(stubHost, wiremockPort)
  }

  override protected def afterAll() = {
    wireMockServer.stop()
    super.afterAll()
  }

  override protected def beforeEach() = {
    super.beforeEach()
    WireMock.reset()
  }
}
