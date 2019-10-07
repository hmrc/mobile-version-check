package uk.gov.hmrc.mobileversioncheck.domain

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Reads, Writes}


sealed trait State

case object OPEN extends State
case object PRELIVE extends State
case object EMERGENCY extends State

object State {
  val reads: Reads[State] = new Reads[State] {
    override def reads(json: JsValue): JsResult[State] = json match {
      case JsString("OPEN") => JsSuccess(OPEN)
      case JsString("PRELIVE") => JsSuccess(PRELIVE)
      case JsString("EMERGENCY") => JsSuccess(EMERGENCY)
      case _ => JsError("unknown state")
    }
  }

  val writes: Writes[State] = new Writes[State] {
    override def writes(state: State): JsString = state match {
      case OPEN => JsString("OPEN")
      case EMERGENCY => JsString("EMERGENCY")
      case PRELIVE => JsString("PRELIVE")
    }
  }

  implicit val formats: Format[State] = Format(reads, writes)
}
