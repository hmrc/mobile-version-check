/*
 * Copyright 2018 HM Revenue & Customs
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

import play.api.libs.json.Json.{parse, toJson}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.Android
import uk.gov.hmrc.mobileversioncheck.domain.{DeviceVersion, PreFlightCheckResponse}
import uk.gov.hmrc.mobileversioncheck.service.VersionCheckService

import scala.concurrent.{ExecutionContext, Future}

class LiveVersionCheckControllerSpec extends BaseControllerSpec{
  val service: VersionCheckService = mock[VersionCheckService]
  val controller = new LiveVersionCheckController(service)

  def mockServiceCall(upgrade: Boolean, optionalJourneyId: Option[String], deviceVersion: DeviceVersion = iOSVersion): Unit =
    (service.versionCheck(_: DeviceVersion, _: Option[String])(_: HeaderCarrier, _: ExecutionContext)).
      expects(deviceVersion, optionalJourneyId, *, *).returning( Future successful PreFlightCheckResponse(upgrade))

  "version check" should {
    "return upgradeRequired true when a journey id is supplied" in {
      mockServiceCall(upgrade = true, Some(journeyId))

      val result = await(controller.versionCheck(Some(journeyId))(iOSRequestWithValidHeaders))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe parse(upgradeRequiredResult)
    }

    "return upgradeRequired false when a journey id is supplied" in {
      mockServiceCall(upgrade = false, Some(journeyId))

      val result = await(controller.versionCheck(Some(journeyId))(iOSRequestWithValidHeaders))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe parse(upgradeNotRequiredResult)
    }

    "return upgradeRequired true when no journey id is supplied" in {
      mockServiceCall(upgrade = true, None)

      val result = await(controller.versionCheck(None)(iOSRequestWithValidHeaders))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe parse(upgradeRequiredResult)
    }

    "return upgradeRequired false when no journey id is supplied" in {
      mockServiceCall(upgrade = false, None)

      val result = await(controller.versionCheck(None)(iOSRequestWithValidHeaders))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe parse(upgradeNotRequiredResult)
    }

    "return upgradeRequired result for android OS" in {
      val androidVersion = DeviceVersion(Android, "0.1")

      mockServiceCall(upgrade = true, None, androidVersion)

      val result = await(controller.versionCheck(None)(FakeRequest().withBody(toJson(androidVersion)).withHeaders(acceptJsonHeader)))

      status(result) shouldBe 200
      contentAsJson(result) shouldBe parse(upgradeRequiredResult)
    }

    "require the accept header" in {
      val result = await(controller.versionCheck(None)(iOSRequest))
      status(result) shouldBe 406
    }

    "require an app OS" in {
      val invalidRequest = FakeRequest().withBody(parse("""{ "version": "1.0" }""")).withHeaders(acceptJsonHeader)
      val result = await(controller.versionCheck(None)(invalidRequest))
      status(result) shouldBe 400
    }

    "require a version" in {
      val invalidRequest = FakeRequest().withBody(parse("""{ "os": "iOS" }""")).withHeaders(acceptJsonHeader)
      val result = await(controller.versionCheck(None)(invalidRequest))
      status(result) shouldBe 400
    }
  }
}
