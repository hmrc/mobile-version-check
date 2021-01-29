/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.mobileversioncheck.controllers

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.Json.{parse, toJson}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.Android
import uk.gov.hmrc.mobileversioncheck.domain.types.ModelTypes.JourneyId
import uk.gov.hmrc.mobileversioncheck.domain.{AppState, DeviceVersion}
import uk.gov.hmrc.mobileversioncheck.service.VersionCheckService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class LiveVersionCheckControllerSpec extends BaseControllerSpec {
  val service: VersionCheckService = mock[VersionCheckService]
  val controller = new LiveVersionCheckController(service, stubControllerComponents())

  def mockServiceCall(
    upgradeRequired:   Boolean,
    optionalJourneyId: Option[JourneyId],
    deviceVersion:     DeviceVersion = iOSVersion
  ): Unit =
    (service
      .versionCheck(_: DeviceVersion, _: JourneyId, _: String)(_: HeaderCarrier, _: ExecutionContext))
      .expects(deviceVersion, journeyId, *, *, *)
      .returning(Future successful upgradeRequired)

  def mockAppStateCall(
    appState:       Option[AppState],
    callingService: String,
    deviceVersion:  DeviceVersion = iOSVersion
  ): Unit =
    (service
      .appState(_: String, _: DeviceVersion)(_: HeaderCarrier, _: ExecutionContext))
      .expects(callingService, deviceVersion, *, *)
      .returning(Future successful appState)

  val scenarios = Table(
    ("testName", "callingService"),
    ("As NGC Service", ngcService),
    ("As RDS Service", rdsService)
  )

  forAll(scenarios) { (testName: String, callingService: String) =>
    s"return upgradeRequired true when a journey id is supplied $testName" in {
      mockServiceCall(upgradeRequired = true, Some(journeyId))
      mockAppStateCall(openAppState, callingService)

      val result = controller.versionCheck(journeyId, callingService)(iOSRequestWithValidHeaders)

      status(result) mustBe 200
      contentAsJson(result) mustBe parse(upgradeRequiredResultRds)
    }

    s"return upgradeRequired false when a journey id is supplied $testName" in {
      mockServiceCall(upgradeRequired = false, Some(journeyId))
      mockAppStateCall(openAppState, callingService)

      val result = controller.versionCheck(journeyId, callingService)(iOSRequestWithValidHeaders)

      status(result) mustBe 200
      contentAsJson(result) mustBe parse(upgradeNotRequiredResultRds)
    }

    s"return upgradeRequired true when no journey id is supplied $testName" in {
      mockServiceCall(upgradeRequired = true, None)
      mockAppStateCall(openAppState, callingService)

      val result = controller.versionCheck(journeyId, callingService)(iOSRequestWithValidHeaders)

      status(result) mustBe 200
      contentAsJson(result) mustBe parse(upgradeRequiredResultRds)
    }

    s"return upgradeRequired false when no journey id is supplied $testName" in {
      mockServiceCall(upgradeRequired = false, None)
      mockAppStateCall(openAppState, callingService)

      val result = controller.versionCheck(journeyId, callingService)(iOSRequestWithValidHeaders)

      status(result) mustBe 200
      contentAsJson(result) mustBe parse(upgradeNotRequiredResultRds)
    }

    s"return upgradeRequired result for android OS $testName" in {
      val androidVersion = DeviceVersion(Android, "0.1")

      mockServiceCall(upgradeRequired                              = true, None, androidVersion)
      mockAppStateCall(openAppState, callingService, deviceVersion = androidVersion)

      val result = controller.versionCheck(journeyId, callingService)(
        FakeRequest().withBody(toJson(androidVersion)).withHeaders(acceptJsonHeader)
      )

      status(result) mustBe 200
      contentAsJson(result) mustBe parse(upgradeRequiredResultRds)
    }

    s"require the accept header $testName" in {
      val result = controller.versionCheck(journeyId, callingService)(iOSRequest)
      status(result) mustBe 406
    }

    s"require an app OS $testName" in {
      val invalidRequest = FakeRequest().withBody(parse("""{ "version": "1.0" }""")).withHeaders(acceptJsonHeader)
      val result         = controller.versionCheck(journeyId, callingService)(invalidRequest)
      status(result) mustBe 400
    }

    s"require a version $testName" in {
      val invalidRequest = FakeRequest().withBody(parse("""{ "os": "iOS" }""")).withHeaders(acceptJsonHeader)
      val result         = controller.versionCheck(journeyId, callingService)(invalidRequest)
      status(result) mustBe 400
    }
  }
}
