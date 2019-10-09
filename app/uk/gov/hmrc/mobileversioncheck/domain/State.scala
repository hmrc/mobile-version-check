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

import play.api.libs.json._

sealed trait State { val value: String }

case object OPEN extends State { val value      = "OPEN" }
case object PRELIVE extends State { val value   = "PRELIVE" }
case object EMERGENCY extends State { val value = "EMERGENCY" }

object State {
  val reads: Reads[State] = new Reads[State] {
    override def reads(json: JsValue): JsResult[State] = json.toString().toUpperCase match {
      case "OPEN"      => JsSuccess(OPEN)
      case "PRELIVE"   => JsSuccess(PRELIVE)
      case "EMERGENCY" => JsSuccess(EMERGENCY)
      case _           => JsError("unknown state")
    }
  }

  val writes: Writes[State] = new Writes[State] {
    override def writes(state: State): JsString = state match {
      case OPEN      => JsString("OPEN")
      case EMERGENCY => JsString("EMERGENCY")
      case PRELIVE   => JsString("PRELIVE")
    }
  }

  implicit val formats: Format[State] = Format(reads, writes)
}
