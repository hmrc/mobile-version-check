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

import java.util.UUID.randomUUID

import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.libs.json.JsValue
import play.api.libs.json.Json.{parse, toJson}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, _}
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.Android

import scala.concurrent.ExecutionContext.Implicits.global

class SandboxVersionCheckControllerSpec extends BaseControllerSpec {
  val controller = new SandboxVersionCheckController(stubControllerComponents())

  val sandboxControlHeader = "SANDBOX-CONTROL"
  val androidVersion       = DeviceVersion(Android, "0.1")

  val requestWithoutOS: FakeRequest[JsValue] =
    FakeRequest().withBody(parse("""{ "version": "1.0" }""")).withHeaders(acceptJsonHeader)

  val requestWithoutVersion: FakeRequest[JsValue] =
    FakeRequest().withBody(parse("""{ "os": "iOS" }""")).withHeaders(acceptJsonHeader)

  val androidRequestWithAcceptHeader: FakeRequest[JsValue] =
    FakeRequest().withBody(toJson(androidVersion)).withHeaders(acceptJsonHeader)

  val scenarios = Table(
    ("testName", "callingService"),
    ("As NGC Service", ngcService)
  )

  forAll(scenarios) { (testName: String, callingService: String) =>
    s"version check without SANDBOX-CONTROL header specified $testName" should {
      s"return upgradeRequired false when a journey id is supplied $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iOSRequestWithValidHeaders)
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeNotRequiredResultNgc)
      }

      s"return upgradeRequired result for android OS $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(androidRequestWithAcceptHeader)
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeNotRequiredResultNgc)
      }

      s"require the accept header $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iOSRequest)
        status(result) mustBe 406
      }

      s"require an app OS $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(requestWithoutOS)
        status(result) mustBe 400
      }

      s"require a version $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(requestWithoutVersion)
        status(result) mustBe 400
      }
    }

    s"version check with random SANDBOX-CONTROL header supplied $testName" should {
      val sandboxHeader                            = sandboxControlHeader -> randomUUID().toString
      val iosRequestWithRandomSandboxControlHeader = iOSRequestWithValidHeaders.withHeaders(sandboxHeader)

      s"return upgradeRequired false when a journey id is supplied $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iosRequestWithRandomSandboxControlHeader)
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeNotRequiredResultNgc)
      }

      s"return upgradeRequired result for android OS $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(androidRequestWithAcceptHeader.withHeaders(sandboxHeader))
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeNotRequiredResultNgc)
      }

      s"require the accept header $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iOSRequest.withHeaders(sandboxHeader))
        status(result) mustBe 406
      }

      s"require an app OS $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(requestWithoutOS.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }

      s"require a version $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(requestWithoutVersion.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }
    }

    s"version check with 'ERROR-500' SANDBOX-CONTROL header supplied $testName" should {
      val sandboxHeader                            = sandboxControlHeader -> "ERROR-500"
      val iosRequestWithRandomSandboxControlHeader = iOSRequestWithValidHeaders.withHeaders(sandboxHeader)

      s"return 500 false when a journey id is supplied $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iosRequestWithRandomSandboxControlHeader)
        status(result) mustBe 500
      }

      s"return upgradeRequired result for android OS $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(androidRequestWithAcceptHeader.withHeaders(sandboxHeader))
        status(result) mustBe 500
      }

      s"require the accept header $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iOSRequest.withHeaders(sandboxHeader))
        status(result) mustBe 406
      }

      s"require an app OS $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(requestWithoutOS.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }

      s"require a version $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(requestWithoutVersion.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }
    }

    s"version check with 'UPGRADE-REQUIRED' SANDBOX-CONTROL header supplied $testName" should {
      val sandboxHeader                            = sandboxControlHeader -> "UPGRADE-REQUIRED"
      val iosRequestWithRandomSandboxControlHeader = iOSRequestWithValidHeaders.withHeaders(sandboxHeader)

      s"return upgradeRequired false when a journey id is supplied $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iosRequestWithRandomSandboxControlHeader)
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeRequiredResultNgc)
      }

      s"return upgradeRequired false when no journey id is supplied $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iosRequestWithRandomSandboxControlHeader)
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeRequiredResultNgc)
      }

      s"return upgradeRequired result for android OS $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(androidRequestWithAcceptHeader.withHeaders(sandboxHeader))
        status(result) mustBe 200
        contentAsJson(result) mustBe parse(upgradeRequiredResultNgc)
      }

      s"require the accept header $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(iOSRequest.withHeaders(sandboxHeader))
        status(result) mustBe 406
      }

      s"require an app OS $testName" in {
        val result = controller.versionCheck(journeyId, callingService)(requestWithoutOS.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }

      s"require a version $testName" in {
        val result =
          controller.versionCheck(journeyId, callingService)(requestWithoutVersion.withHeaders(sandboxHeader))
        status(result) mustBe 400
      }
    }
  }
}
