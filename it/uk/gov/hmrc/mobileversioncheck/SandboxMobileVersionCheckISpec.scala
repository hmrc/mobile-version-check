package uk.gov.hmrc.mobileversioncheck

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.iOS
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class SandboxMobileVersionCheckISpec extends BaseISpec {
  val mobileIdHeader: (String, String) = "X-MOBILE-USER-ID" -> "208606423740"

  def request(service: String): WSRequest =
    wsUrl(s"/mobile-version-check?journeyId=journeyId&service=$service").addHttpHeaders(acceptJsonHeader, mobileIdHeader)

  val scenarios = Table(
    ("testName", "callingService"),
    ("As NGC Service", ngcService),
    ("As RDS Service", rdsService)
  )

  forAll(scenarios) { (testName: String, callingService: String) =>
    s"POST /sandbox/mobile-version-check $testName" should {
      s"respect the sandbox headers and return true when the UPGRADE-REQUIRED control is specified $testName" in {
        val response =
          request(callingService).addHttpHeaders("SANDBOX-CONTROL" -> "UPGRADE-REQUIRED").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

        response.status                                   shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean]   shouldBe true
        (response.json \ "appState" \ "state").as[String] shouldBe "OPEN"
      }

      s"respect the sandbox headers and return false when no control is specified $testName" in {
        val response = request(callingService).post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
      }

      s"respect the sandbox headers and return a 500 error when the ERROR-500 control is specified $testName" in {
        val response = request(callingService).addHttpHeaders("SANDBOX-CONTROL" -> "ERROR-500").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

        response.status shouldBe 500
      }
      s"respect the sandbox headers and return correct appState when the PRELIVE-APPSTATE control is specified $testName" in {
        val response =
          request(callingService).addHttpHeaders("SANDBOX-CONTROL" -> "PRELIVE-APPSTATE").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

        response.status                                   shouldBe 200
        (response.json \ "appState" \ "state").as[String] shouldBe "PRELIVE"
      }
      s"respect the sandbox headers and return correct appState when the EMERGENCY-APPSTATE control is specified $testName" in {
        val response =
          request(callingService).addHttpHeaders("SANDBOX-CONTROL" -> "EMERGENCY-APPSTATE").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

        response.status                                   shouldBe 200
        (response.json \ "appState" \ "state").as[String] shouldBe "EMERGENCY"
      }
    }
  }
}
