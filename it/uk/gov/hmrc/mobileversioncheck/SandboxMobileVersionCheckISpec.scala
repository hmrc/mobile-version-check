package uk.gov.hmrc.mobileversioncheck

import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.iOS
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class SandboxMobileVersionCheckISpec extends BaseISpec {
  val mobileIdHeader: (String, String) = "X-MOBILE-USER-ID" -> "208606423740"

  def request: WSRequest = wsUrl(s"/mobile-version-check?journeyId=journeyId").addHttpHeaders(acceptJsonHeader, mobileIdHeader)

  "POST /sandbox/mobile-version-check" should {
    "respect the sandbox headers and return true when the UPGRADE-REQUIRED control is specified" in {
      val response = request.addHttpHeaders("SANDBOX-CONTROL" -> "UPGRADE-REQUIRED").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

      response.status                                 shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe true
    }

    "respect the sandbox headers and return false when no control is specified" in {
      val response = request.post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

      response.status                                 shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }

    "respect the sandbox headers and return a 500 error when the ERROR-500 control is specified" in {
      val response = request.addHttpHeaders("SANDBOX-CONTROL" -> "ERROR-500").post(toJson(DeviceVersion(iOS, "3.0.8"))).futureValue

      response.status shouldBe 500
    }
  }
}
