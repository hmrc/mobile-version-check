package uk.gov.hmrc.mobileversioncheck

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class LiveMobileVersionCheckStateActiveISpec extends BaseISpec {

  "GET /ping/ping" should {
    def request: WSRequest = wsUrl(s"/ping/ping").addHttpHeaders(acceptJsonHeader)

    "respond with 200" in {
      val response = request.get().futureValue
      response.status shouldBe 200
    }
  }

  private def alterDeviceVersion(
    lowestAcceptedVersion: String,
    valueChange:           Int
  ): String = {
    val version = Version(lowestAcceptedVersion)
    version.copy(revision = version.revision + valueChange).toString
  }

  val scenarios = Table(
    ("testName", "callingService", "lowestAcceptedIosVersion", "lowestAcceptedAndroidVersion"),
    ("As NGC Service", ngcService, "3.0.7", "5.0.22")
  )

  forAll(scenarios) {
    (testName:                     String,
     callingService:               String,
     lowestAcceptedIosVersion:     String,
     lowestAcceptedAndroidVersion: String) =>
      s"POST /mobile-version-check $testName" should {
        def request: WSRequest =
          wsUrl(s"/mobile-version-check/$callingService?journeyId=dd1ebd2e-7156-47c7-842b-8308099c5e75")

        s"indicate that an upgrade is required for a version below the lower bound version of iOS $testName" in {
          val response =
            request
              .addHttpHeaders(acceptJsonHeader)
              .post(toJson(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, -1))))
              .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe true
        }

        s"indicate that an upgrade is not required for a version equal to the lower bound version of iOS $testName" in {
          val response = request
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(iOS, lowestAcceptedIosVersion)))
            .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        }

        s"indicate that an upgrade is not required for a version above the lower bound version of iOS $testName" in {
          val response =
            request
              .addHttpHeaders(acceptJsonHeader)
              .post(toJson(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, 1))))
              .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        }

        s"indicate that an upgrade is required for a version below the lower bound version of android $testName" in {
          val response = request
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, -1))))
            .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe true
        }

        s"indicate that an upgrade is not required for a version equal to the lower bound version of android $testName" in {
          val response = request
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, lowestAcceptedAndroidVersion)))
            .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        }

        s"indicate that an upgrade is not required for a version above the lower bound version of android $testName" in {
          val response = request
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1))))
            .futureValue

          response.status                                 shouldBe 200
          (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        }

        s"return 400 BAD REQUEST if journeyId is not supplied $testName" in {
          val response = wsUrl(s"/mobile-version-check/$callingService")
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1))))
            .futureValue

          response.status shouldBe 400
        }

        s"return 400 BAD REQUEST if journeyId is invalid$testName" in {
          val response = wsUrl(s"/mobile-version-check/$callingService?journeyId=ThisIsAnInvalidJourneyId")
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1))))
            .futureValue

          response.status shouldBe 400
        }
      }
  }
}
