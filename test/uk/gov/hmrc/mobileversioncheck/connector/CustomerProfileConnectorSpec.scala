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

package uk.gov.hmrc.mobileversioncheck.connector

import play.api.libs.json.Json.parse
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http._
import uk.gov.hmrc.mobileversioncheck.BaseSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class CustomerProfileConnectorSpec extends BaseSpec{
  val http: CorePost = mock[CorePost]
  val connector = new CustomerProfileConnector(http, "someUrl")

  def mockHttpPost(result: Future[JsValue]): Unit =
    (http.POST(_: String, _: JsValue, _: Seq[(String,String)])
      (_: Writes[JsValue], _: HttpReads[JsValue], _: HeaderCarrier, _: ExecutionContext)).expects(*,*,*,*,*,*,*).returns(result)

  def upgradeRequired(upgrade: Boolean): JsValue = parse(s"""{ "upgrade": $upgrade }""")

  "version check" should {
    "return upgrade true when this is returned by customer profile" in  {
      mockHttpPost(upgradeRequired(true))
      await(connector.versionCheck(iOSVersion, hc)) shouldBe true
    }

    "return upgrade false when this is returned by customer profile" in  {
      mockHttpPost(upgradeRequired(false))
      await(connector.versionCheck(iOSVersion, hc)) shouldBe false
    }
  }
}
