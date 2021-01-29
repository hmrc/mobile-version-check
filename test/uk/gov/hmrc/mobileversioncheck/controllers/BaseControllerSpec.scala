/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.mobileversioncheck.controllers

import play.api.libs.json.JsValue
import play.api.libs.json.Json.toJson
import play.api.test.FakeRequest
import uk.gov.hmrc.mobileversioncheck.BaseSpec
import uk.gov.hmrc.mobileversioncheck.domain.{ACTIVE, AppState}

trait BaseControllerSpec extends BaseSpec {
  val iOSVersionJson:   JsValue              = toJson(iOSVersion)
  val acceptJsonHeader: (String, String)     = "Accept" -> "application/vnd.hmrc.1.0+json"
  val iOSRequest:       FakeRequest[JsValue] = FakeRequest().withBody(iOSVersionJson)

  val iOSRequestWithValidHeaders: FakeRequest[JsValue] =
    FakeRequest().withBody(iOSVersionJson).withHeaders(acceptJsonHeader)
  val upgradeNotRequiredResultRds = s"""{"upgradeRequired":false,"appState":{"state":"ACTIVE"}}"""
  val upgradeRequiredResultRds    = s"""{"upgradeRequired":true,"appState":{"state":"ACTIVE"}}"""

  val upgradeNotRequiredPreliveResultRds =
    s"""{"upgradeRequired":false,"appState":{"state":"INACTIVE","endDate":"2019-11-01T00:00:00Z"}}"""

  val upgradeNotRequiredEmergencyResultRds =
    s"""{"upgradeRequired":false,"appState":{"state":"SHUTTERED","endDate":"2020-01-01T00:00:00Z"}}"""
  val upgradeNotRequiredResultNgc          = s"""{"upgradeRequired":false}"""
  val upgradeRequiredResultNgc             = s"""{"upgradeRequired":true}"""
  val upgradeNotRequiredPreliveResultNgc   = s"""{"upgradeRequired":false}"""
  val upgradeNotRequiredEmergencyResultNgc = s"""{"upgradeRequired":false}"""
  val openAppState                         = Some(AppState(ACTIVE, None))
}
