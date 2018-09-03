package uk.gov.hmrc.mobileversioncheck.support

import org.scalatest.{Matchers, OptionValues}
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.play.test.UnitSpec

class BaseISpec extends UnitSpec with Matchers with OptionValues with WsScalaTestClient with GuiceOneServerPerSuite with WireMockSupport {
  override implicit lazy val app: Application = appBuilder.build()

  protected val acceptJsonHeader: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"

  def config: Map[String, Any] = Map(
    "auditing.enabled" -> true,
    "microservice.services.auth.port" -> wireMockPort,
    "microservice.services.datastream.port" -> wireMockPort,
    "microservice.services.customer-profile.port" -> wireMockPort,
    "auditing.consumer.baseUri.port" -> wireMockPort)

  protected def appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().configure(config)

  protected implicit lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
}
