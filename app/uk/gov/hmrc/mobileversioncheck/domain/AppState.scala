package uk.gov.hmrc.mobileversioncheck.domain

import play.api.libs.json.Json.format
import play.api.libs.json.OFormat



case class AppState(state: State, message: String)
object AppState {
  implicit val appState: OFormat[AppState] = {
    format[AppState]
  }
}