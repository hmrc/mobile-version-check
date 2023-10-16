package uk.gov.hmrc.mobileversioncheck.support

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient

abstract class BaseISpec
    extends AnyWordSpecLike
    with Matchers
    with OptionValues
    with WsScalaTestClient
    with GuiceOneServerPerSuite
    with WireMockSupport
    with ScalaFutures {

  val ngcService = "ngc"

  override implicit lazy val app: Application = appBuilder.build()

  protected val acceptJsonHeader: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"

  def config: Map[String, Any] = Map(
    "auditing.enabled"                      -> true,
    "microservice.services.auth.port"       -> wireMockPort,
    "microservice.services.datastream.port" -> wireMockPort,
    "auditing.consumer.baseUri.port"        -> wireMockPort,
    "approvedAppVersions.ngc.ios"           -> "[1.0.0,]",
    "approvedAppVersions.ngc.android"       -> "[1.0.0,]"
  )

  protected def appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().configure(config)

  protected implicit lazy val wsClient: WSClient = app.injector.instanceOf[WSClient]
}
