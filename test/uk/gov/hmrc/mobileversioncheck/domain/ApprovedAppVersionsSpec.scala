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

package uk.gov.hmrc.mobileversioncheck.domain

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory.parseString
import uk.gov.hmrc.play.test.UnitSpec

class ApprovedAppVersionsSpec extends UnitSpec {

  val specConfig: Config = parseString(
    """approvedAppVersions {
      |  ios = "[0.0.1,)"
      |  android = "[0.0.1,)"
      |}
      | """.stripMargin)

  lazy val approvedAppVersions: ApprovedAppVersions = new ApprovedAppVersions {
    override lazy val config: Config = specConfig
  }

  "ApprovedAppVersions" should {
    "be loaded from config" in {
      approvedAppVersions.appVersion.ios shouldBe VersionRange("[0.0.1,)")
    }
  }
}
