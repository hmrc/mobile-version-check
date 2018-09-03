package uk.gov.hmrc.mobileversioncheck

import play.api.libs.json.Json.toJson
import play.api.libs.json._
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.iOS
import uk.gov.hmrc.mobileversioncheck.stubs.CustomerProfileStub.upgradeRequired
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class MobileVersionCheckISpec extends BaseISpec{
  "GET /ping/ping" should {
    def request: WSRequest = wsUrl(s"/ping/ping").withHeaders(acceptJsonHeader)

    "respond with 200" in {
      val response = await(request.get())
      response.status shouldBe 200
    }
  }

  "POST /mobile-version-check" should {
    def request: WSRequest = wsUrl(s"/mobile-version-check")

    val version: JsValue = toJson(DeviceVersion(iOS, "0.1"))

    "indicate that an upgrade is required without nan auth check" in {
      upgradeRequired(upgrade = true)

      val response = await(request.withHeaders(acceptJsonHeader).post(version))

      response.status shouldBe 200
      ( response.json \ "upgradeRequired" ).as[Boolean] shouldBe true
    }

    "indicate that an upgrade is not required without nan auth check" in {
      upgradeRequired(upgrade = false)

      val response = await(request.withHeaders(acceptJsonHeader).post(version))

      response.status shouldBe 200
      ( response.json \ "upgradeRequired" ).as[Boolean] shouldBe false
    }
  }
}
