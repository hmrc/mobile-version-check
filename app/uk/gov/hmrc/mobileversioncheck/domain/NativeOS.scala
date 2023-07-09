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

package uk.gov.hmrc.mobileversioncheck.domain

import play.api.libs.json._

trait NativeOS

object NativeOS {

  case object iOS extends NativeOS {
    override def toString: String = "ios"
  }

  case object Android extends NativeOS {
    override def toString: String = "android"
  }

  val reads: Reads[NativeOS] = new Reads[NativeOS] {

    override def reads(json: JsValue): JsResult[NativeOS] = json match {
      case JsString("ios")     => JsSuccess(iOS)
      case JsString("android") => JsSuccess(Android)
      case _                   => JsError("unknown os")
    }
  }

  val writes: Writes[NativeOS] = new Writes[NativeOS] {

    override def writes(os: NativeOS): JsString = os match {
      case `iOS`   => JsString("ios")
      case Android => JsString("android")
    }
  }

  implicit val formats: Format[NativeOS] = Format(reads, writes)
}
