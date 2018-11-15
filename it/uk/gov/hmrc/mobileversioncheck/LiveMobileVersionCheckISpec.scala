package uk.gov.hmrc.mobileversioncheck

import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class LiveMobileVersionCheckISpec extends BaseISpec {
  "GET /ping/ping" should {
    def request: WSRequest = wsUrl(s"/ping/ping").withHeaders(acceptJsonHeader)

    "respond with 200" in {
      val response = await(request.get())
      response.status shouldBe 200
    }
  }

  "POST /mobile-version-check" should {
    def request: WSRequest = wsUrl(s"/mobile-version-check")

    "indicate that an upgrade is required for a version below the lower bound version of iOS" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, "3.0.6"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe true
    }

    "indicate that an upgrade is not required for a version equal to the lower bound version of iOS" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, "3.0.7"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }

    "indicate that an upgrade is not required for a version above the lower bound version of iOS" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, "3.0.8"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }

    "indicate that an upgrade is required for a version below the lower bound version of android" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(Android, "5.0.21"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe true
    }

    "indicate that an upgrade is not required for a version equal to the lower bound version of android" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(Android, "5.0.22"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }

    "indicate that an upgrade is not required for a version above the lower bound version of android" in {
      val response = await(request.withHeaders(acceptJsonHeader).post(toJson(DeviceVersion(Android, "5.0.23"))))

      response.status shouldBe 200
      (response.json \ "upgradeRequired").as[Boolean] shouldBe false
    }
  }
}
