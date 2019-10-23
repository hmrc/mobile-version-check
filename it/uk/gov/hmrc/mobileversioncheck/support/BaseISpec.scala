package uk.gov.hmrc.mobileversioncheck.support

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, OptionValues, WordSpecLike}
import org.scalatestplus.play.WsScalaTestClient
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import uk.gov.hmrc.integration.ServiceSpec
import uk.gov.hmrc.mobileversioncheck.domain._

abstract class BaseISpec
    extends WordSpecLike
    with ServiceSpec
    with Matchers
    with OptionValues
    with WsScalaTestClient
    with GuiceOneServerPerSuite
    with WireMockSupport
    with ScalaFutures {

  def state: State = ACTIVE

  val ngcService = "ngc"
  val rdsService = "rds"

  override implicit lazy val app: Application = appBuilder.build()

  protected val acceptJsonHeader: (String, String) = "Accept" -> "application/vnd.hmrc.1.0+json"

  def getExpectedResponse(appState: Option[AppState], callingService: String): Option[AppState] =
    callingService match {
      case "ngc" => None
      case "rds" => appState
    }

  def config: Map[String, Any] = {
    val baseConfig = Map(
      "auditing.enabled"                      -> true,
      "microservice.services.auth.port"       -> wireMockPort,
      "microservice.services.datastream.port" -> wireMockPort,
      "auditing.consumer.baseUri.port"        -> wireMockPort
    )

    if (state == ACTIVE) {
      val conf = baseConfig + ("approvedAppVersions.ngc.ios" -> "[1.0.0,]",
      "approvedAppVersions.ngc.android" -> "[1.0.0,]",
      "approvedAppVersions.rds.ios"     -> "[1.0.0,]",
      "approvedAppVersions.rds.android" -> "[1.0.0,]",
      "rds.state"                       -> "ACTIVE")
      conf
    } else if (state == INACTIVE) {
      val conf = baseConfig + ("approvedAppVersions.ngc.ios" -> "[1.0.0,]",
      "approvedAppVersions.ngc.android" -> "[1.0.0,]",
      "approvedAppVersions.rds.ios"     -> "[1.0.0,]",
      "approvedAppVersions.rds.android" -> "[1.0.0,]",
      "rds.state"                       -> "INACTIVE",
      "rds.endDate"                     -> "2019-11-01T00:00:00Z")
      conf
    } else if (state == SHUTTERED) {
      val conf = baseConfig + ("approvedAppVersions.ngc.ios" -> "[1.0.0,]",
      "approvedAppVersions.ngc.android" -> "[1.0.0,]",
      "approvedAppVersions.rds.ios"     -> "[1.0.0,]",
      "approvedAppVersions.rds.android" -> "[1.0.0,]",
      "rds.state"                       -> "SHUTTERED",
      "rds.endDate"                     -> "2019-11-01T00:00:00Z")
      conf
    } else {
      baseConfig
    }

  }

  protected def appBuilder: GuiceApplicationBuilder = new GuiceApplicationBuilder().configure(config)

  protected implicit lazy val wsClient: WSClient    = app.injector.instanceOf[WSClient]
  override def externalServices:        Seq[String] = Seq()
}
