package uk.gov.hmrc.mobileversioncheck

import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.stubs.CustomerProfileStub.upgradeRequired
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class LiveMobileVersionCheckISpec extends BaseISpec{
  "GET /ping/ping" should {
    def request: WSRequest = wsUrl(s"/ping/ping").withHeaders(acceptJsonHeader)

    "respond with 200" in {
      val response = await(request.get())
      response.status shouldBe 200
    }
  }

  "POST /mobile-version-check" should {
    def request: WSRequest = wsUrl(s"/mobile-version-check")

    "indicate that an upgrade is required without an auth check" in {
      upgradeRequired(upgrade = true)

      val response = await(request.withHeaders(acceptJsonHeader).post(deviceVersion))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe true
    }

    "indicate that an upgrade is not required without an auth check" in {
      upgradeRequired(upgrade = false)

      val response = await(request.withHeaders(acceptJsonHeader).post(deviceVersion))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }
  }
}
