/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.mobileversioncheck.service

import org.scalamock.handlers.CallHandler3
import org.scalamock.matchers.MatcherBase
import org.scalatest.prop.TableDrivenPropertyChecks.forAll
import org.scalatest.prop.Tables.Table
import play.api.{ConfigLoader, Configuration}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.BaseSpec
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class VersionCheckServiceSpec extends BaseSpec {
  val appNameConfiguration: Configuration  = mock[Configuration]
  val auditConnector:       AuditConnector = mock[AuditConnector]

  val appName = "mobile-version-check"

  val service = new VersionCheckService(appNameConfiguration, auditConnector)

  def mockAudit(
    transactionName: String,
    detail:          Map[String, String] = Map.empty
  ): CallHandler3[DataEvent, HeaderCarrier, ExecutionContext, Future[AuditResult]] = {
    def dataEventWith(
      auditSource: String,
      auditType:   String,
      tags:        Map[String, String]
    ): MatcherBase =
      argThat { (dataEvent: DataEvent) =>
        dataEvent.auditSource.equals(auditSource) &&
        dataEvent.auditType.equals(auditType) &&
        dataEvent.tags.equals(tags) &&
        dataEvent.detail.equals(detail)
      }

    (appNameConfiguration
      .get[String](_: String)(_: ConfigLoader[String]))
      .expects("appName", *)
      .returns(appName)
      .anyNumberOfTimes()

    (auditConnector
      .sendEvent(_: DataEvent)(_: HeaderCarrier, _: ExecutionContext))
      .expects(
        dataEventWith(appName, auditType = "ServiceResponseSent", tags = Map("transactionName" -> transactionName)),
        *,
        *
      )
      .returns(Future successful Success)
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
      s"audit and not require an upgrade for the configured lower bound version of iOS $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
        service
          .versionCheck(DeviceVersion(iOS, lowestAcceptedIosVersion), journeyId, callingService)
          .futureValue mustBe false
      }

      s"audit and require an upgrade below the configured lower bound version of iOS $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
        service
          .versionCheck(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, -1)), journeyId, callingService)
          .futureValue mustBe true
      }

      s"audit and not require an upgrade above the configured lower bound version of iOS $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
        service
          .versionCheck(DeviceVersion(iOS, alterDeviceVersion(lowestAcceptedIosVersion, 1)), journeyId, callingService)
          .futureValue mustBe false
      }

      s"audit and not require an upgrade for the configured lower bound version of android $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
        service
          .versionCheck(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1)),
                        journeyId,
                        callingService)
          .futureValue mustBe false
      }

      s"audit and require an upgrade below the configured lower bound version of android $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
        service
          .versionCheck(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, -1)),
                        journeyId,
                        callingService)
          .futureValue mustBe true
      }

      s"audit and not require an upgrade above the configured lower bound version of android $testName" in {
        mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
        service
          .versionCheck(DeviceVersion(Android, alterDeviceVersion(lowestAcceptedAndroidVersion, 1)),
                        journeyId,
                        callingService)
          .futureValue mustBe false
      }
  }

}
