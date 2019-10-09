package uk.gov.hmrc.mobileversioncheck.support

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.integration.ServiceSpec

class BaseISpec
    extends WordSpecLike
    with ServiceSpec
    with Matchers
    with OptionValues
    with WsScalaTestClient
    with GuiceOneServerPerSuite
    with WireMockSupport
    with ScalaFutures {

  val ngcService = "ngc"
  val rdsService = "rds"

  override implicit lazy val app: Application = appBuilder.build()

  protected val acceptJsonHeader: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"

  def config: Map[String, Any] =
    Map(
      "auditing.enabled"                      -> true,
      "microservice.services.auth.port"       -> wireMockPort,
      "microservice.services.datastream.port" -> wireMockPort,
      "auditing.consumer.baseUri.port"        -> wireMockPort
    )

  protected def appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().configure(config)

  protected implicit lazy val wsClient: WSClient    = app.injector.instanceOf[WSClient]
  override def externalServices:        Seq[String] = Seq()
}
