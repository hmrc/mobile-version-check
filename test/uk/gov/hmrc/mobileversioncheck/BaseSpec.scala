/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.mobileversioncheck

import eu.timepit.refined.auto._
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain.DeviceVersion
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.iOS
import uk.gov.hmrc.mobileversioncheck.domain.types.ModelTypes.JourneyId

trait BaseSpec extends PlaySpec with MockFactory with ScalaFutures {
  implicit lazy val hc: HeaderCarrier = HeaderCarrier()
  val iOSVersion = DeviceVersion(iOS, "0.1")
  val journeyId: JourneyId = "dd1ebd2e-7156-47c7-842b-8308099c5e75"
  val ngcService = "ngc"
}
