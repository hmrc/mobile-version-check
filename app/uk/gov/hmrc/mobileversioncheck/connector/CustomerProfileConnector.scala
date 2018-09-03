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

import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CustomerProfileConnector @Inject()(http: CorePost, @Named("customer-profile") serviceUrl: String){
  def versionCheck(inputRequest: JsValue, hc: HeaderCarrier)(implicit ec: ExecutionContext): Future[Boolean] =  {
    implicit val hcHeaders: HeaderCarrier = hc.withExtraHeaders("Accept" -> "application/vnd.hmrc.1.0+json")
    http.POST[JsValue, JsValue](s"$serviceUrl/profile/native-app/version-check", inputRequest).map(r => (r \ "upgrade").as[Boolean])
  }
}
