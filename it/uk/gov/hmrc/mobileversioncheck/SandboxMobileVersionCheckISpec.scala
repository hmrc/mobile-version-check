package uk.gov.hmrc.mobileversioncheck

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json.toJson
import play.api.libs.ws.WSRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.support.BaseISpec

class SandboxMobileVersionCheckISpec extends BaseISpec {
  val mobileIdHeader: (String, String) = "X-MOBILE-USER-ID" -> "208606423740"

  def request(service: String): WSRequest =
    wsUrl(s"/mobile-version-check/$service?journeyId=dd1ebd2e-7156-47c7-842b-8308099c5e75")
      .addHttpHeaders(acceptJsonHeader, mobileIdHeader)

  val scenarios = Table(
    ("testName", "callingService"),
    ("As NGC Service", ngcService)
  )

  forAll(scenarios) { (testName: String, callingService: String) =>
    s"POST /sandbox/mobile-version-check $testName" should {
      s"respect the sandbox headers and return true when the UPGRADE-REQUIRED control is specified $testName" in {
        val response =
          await(
            request(callingService)
              .addHttpHeaders("SANDBOX-CONTROL" -> "UPGRADE-REQUIRED")
              .post(toJson(DeviceVersion(iOS, "3.0.8")))
          )

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe true
      }

      s"respect the sandbox headers and return false when no control is specified $testName" in {
        val response = await(request(callingService).post(toJson(DeviceVersion(iOS, "3.0.8"))))

        response.status                                 shouldBe 200
        (response.json \ "upgradeRequired").as[Boolean] shouldBe false
      }

      s"respect the sandbox headers and return a 500 error when the ERROR-500 control is specified $testName" in {
        val response = await(
          request(callingService)
            .addHttpHeaders("SANDBOX-CONTROL" -> "ERROR-500")
            .post(toJson(DeviceVersion(iOS, "3.0.8")))
        )

        response.status shouldBe 500
      }

      s"return 400 BAD REQUEST if journeyId is not supplied $testName" in {
        val response = await(
          wsUrl(s"/mobile-version-check/$callingService")
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, "3.0.8")))
        )

        response.status shouldBe 400
      }

      s"return 400 BAD REQUEST if journeyId is invalid$testName" in {
        val response = await(
          wsUrl(s"/mobile-version-check/$callingService?journeyId=ThisIsAnInvalidJourneyId")
            .addHttpHeaders(acceptJsonHeader)
            .post(toJson(DeviceVersion(Android, "3.0.8")))
        )

        response.status shouldBe 400
      }
    }
  }
}
