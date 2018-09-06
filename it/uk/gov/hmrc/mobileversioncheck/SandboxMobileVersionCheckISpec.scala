package uk.gov.hmrc.mobileversioncheck

import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.stubs.CustomerProfileStub.upgradeRequired
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class SandboxMobileVersionCheckISpec extends BaseISpec{
  val mobileHeader: (String, String) = "X-MOBILE-USER-ID" -> "208606423740"
  val sandboxControlHeader: (String, String) = "SANDBOX-CONTROL" -> "UPGRADE-REQUIRED"

  def request: WSRequest = wsUrl(s"/mobile-version-check").withHeaders(acceptJsonHeader, mobileHeader, sandboxControlHeader)

  "POST /sandbox/mobile-version-check" should {
    "respect the sandbox headers and return upgradeRequired == true without an auth check" in {
      upgradeRequired(upgrade = true)

      val response = await(request.post(deviceVersion))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe true
    }
  }
}
