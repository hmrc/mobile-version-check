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

package uk.gov.hmrc.mobileversioncheck.service

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.BaseSpec
import uk.gov.hmrc.mobileversioncheck.connector.CustomerProfileConnector
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class VersionCheckServiceSpec extends BaseSpec {
  val connector: CustomerProfileConnector = mock[CustomerProfileConnector]
  val service = new VersionCheckService(connector)

  def mockConnectorVersionCheck(upgrade: Boolean) =
    (connector.versionCheck(_: DeviceVersion, _: HeaderCarrier)(_: ExecutionContext)).expects(iOSVersion, *, *).returning(Future successful upgrade)

  "version check" should {
    "return upgradeRequired true when a journey id is supplied" in {
      mockConnectorVersionCheck(upgrade = false)
      val result = await(service.versionCheck(iOSVersion, Some(journeyId)))

      result.upgradeRequired shouldBe false
    }

    "return upgradeRequired false when a journey id is supplied" in {
      mockConnectorVersionCheck(upgrade = true)
      val result = await(service.versionCheck(iOSVersion, Some(journeyId)))

      result.upgradeRequired shouldBe true
    }

    "return upgradeRequired true when no journey id is supplied" in {
      mockConnectorVersionCheck(upgrade = false)
      val result = await(service.versionCheck(iOSVersion, None))

      result.upgradeRequired shouldBe false
    }

    "return upgradeRequired false when no journey id is supplied" in {
      mockConnectorVersionCheck(upgrade = true)
      val result = await(service.versionCheck(iOSVersion, None))

      result.upgradeRequired shouldBe true
    }
  }
}
