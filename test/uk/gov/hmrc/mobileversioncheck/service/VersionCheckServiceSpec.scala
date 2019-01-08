/*
 * Copyright 2019 HM Revenue & Customs
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
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.BaseSpec
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class VersionCheckServiceSpec extends BaseSpec {
  val appNameConfiguration: Configuration = mock[Configuration]
  val auditConnector: AuditConnector = mock[AuditConnector]

  val appName = "mobile-version-check"

  val service = new VersionCheckService(appNameConfiguration, auditConnector)

  def mockAudit(transactionName: String, detail: Map[String, String] = Map.empty): CallHandler3[DataEvent, HeaderCarrier, ExecutionContext, Future[AuditResult]] = {
    def dataEventWith(auditSource: String,
                      auditType: String,
                      tags: Map[String, String]): MatcherBase = {
      argThat((dataEvent: DataEvent) => {
        dataEvent.auditSource.equals(auditSource) &&
          dataEvent.auditType.equals(auditType) &&
          dataEvent.tags.equals(tags) &&
          dataEvent.detail.equals(detail)
      })
    }

    (appNameConfiguration.getString(_: String, _: Option[Set[String]])).expects(
      "appName", None).returns(Some(appName)).anyNumberOfTimes()

    (auditConnector.sendEvent(_: DataEvent)(_: HeaderCarrier, _: ExecutionContext)).expects(
      dataEventWith(appName, auditType = "ServiceResponseSent", tags = Map("transactionName" -> transactionName)), *, *).returns(
      Future successful Success)
  }

  "versionCheck" should {
    "audit and not require an upgrade for the configured lower bound version of iOS" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
      await(service.versionCheck(DeviceVersion(iOS, "3.0.7"))) shouldBe false
    }

    "audit and not require an upgrade below the configured lower bound version of iOS" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
      await(service.versionCheck(DeviceVersion(iOS, "3.0.6"))) shouldBe true
    }

    "audit and not require an upgrade above the configured lower bound version of iOS" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "ios"))
      await(service.versionCheck(DeviceVersion(iOS, "3.0.8"))) shouldBe false
    }

    "audit and not require an upgrade for the configured lower bound version of android" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
      await(service.versionCheck(DeviceVersion(Android, "5.0.22"))) shouldBe false
    }

    "audit and not require an upgrade below the configured lower bound version of android" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
      await(service.versionCheck(DeviceVersion(Android, "5.0.21"))) shouldBe true
    }

    "audit and not require an upgrade above the configured lower bound version of android" in {
      mockAudit(transactionName = "upgradeRequired", Map("os" -> "android"))
      await(service.versionCheck(DeviceVersion(Android, "5.0.23"))) shouldBe false
    }
  }

}
