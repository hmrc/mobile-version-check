package uk.gov.hmrc.mobileversioncheck

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class LiveMobileVersionCheckStateActiveISpec extends BaseISpec {
  override def state: State = ACTIVE

  "GET /ping/ping" should {
    def request: WSRequest = wsUrl(s"/ping/ping").addHttpHeaders(acceptJsonHeader)

    "respond with 200" in {
      val response = request.get().futureValue
      response.status shouldBe 200
    }
  }
  private def alterDeviceVersion(lowestAcceptedVersion: String, valueChange: Int): String = {
    val version = Version(lowestAcceptedVersion)
    version.copy(revision = version.revision + valueChange).toString
  }

  val scenarios = Table(
    ("testName", "callingService", "lowestAcceptedIosVersion", "lowestAcceptedIosVersion"),
    ("As NGC Service", ngcService, "3.0.7", "5.0.22"),
    ("As RDS Service", rdsService, "4.0.7", "6.0.22")
  )

  forAll(scenarios) { (testName: String, callingService: String, lowestAcceptedIosVersion: String, lowestAcceptedAndroidVersion: String) =>
    s"POST /mobile-version-check $testName" should {
      def request: WSRequest = wsUrl(s"/mobile-version-check?journeyId=journeyId&service=$callingService")

      s"indicate that an upgrade is required for a version below the lower bound version of iOS $testName" in {
        val response =
          request.addHttpHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, -1)))).futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe true
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }

      s"indicate that an upgrade is not required for a version equal to the lower bound version of iOS $testName" in {
        val response = request.addHttpHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, lowestAcceptedIosVersion))).futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }

      s"indicate that an upgrade is not required for a version above the lower bound version of iOS $testName" in {
        val response =
          request.addHttpHeaders(acceptJsonHeader).post(toJson(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, 1)))).futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }

      s"indicate that an upgrade is required for a version below the lower bound version of android $testName" in {
        val response = request
          .addHttpHeaders(acceptJsonHeader)
          .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, -1))))
          .futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe true
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }

      s"indicate that an upgrade is not required for a version equal to the lower bound version of android $testName" in {
        val response = request.addHttpHeaders(acceptJsonHeader).post(toJson(DeviceVersion(Android, lowestAcceptedAndroidVersion))).futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }

      s"indicate that an upgrade is not required for a version above the lower bound version of android $testName" in {
        val response = request
          .addHttpHeaders(acceptJsonHeader)
          .post(toJson(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1))))
          .futureValue

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
        (response.json \ "appState").asOpt[AppState]    shouldBe getExpectedResponse(Some(AppState(ACTIVE, None)), callingService)
      }
    }
  }
}
