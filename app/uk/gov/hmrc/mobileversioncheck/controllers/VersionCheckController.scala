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

package uk.gov.hmrc.mobileversioncheck.controllers

import com.google.inject.Singleton
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc.{Action, BodyParsers, Request, Result}
import uk.gov.hmrc.api.controllers.HeaderValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain.{DeviceVersion, PreFlightCheckResponse}
import uk.gov.hmrc.mobileversioncheck.service.VersionCheckService
import uk.gov.hmrc.play.bootstrap.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

trait VersionCheckController extends BaseController with HeaderValidator {
  implicit def ec:ExecutionContext

  def versionCheck(journeyId: Option[String] = None): Action[JsValue] = validateAccept(acceptHeaderValidationRules).async(BodyParsers.parse.json) {
    implicit request =>
      request.body.validate[DeviceVersion].fold(
        errors => {
          Logger.warn("Received error with service validate app version: " + errors)
          Future.successful(BadRequest(JsError.toJson(errors)))
        },
        deviceVersion => {
          doVersionCheck(deviceVersion, journeyId)
        }
      )
  }

  def doVersionCheck(deviceVersion: DeviceVersion, journeyId: Option[String])(implicit hc: HeaderCarrier, request: Request[_]): Future[Result]
}

@Singleton
class LiveVersionCheckController @Inject()(val service: VersionCheckService)(implicit val ec: ExecutionContext) extends VersionCheckController {
  override def doVersionCheck(deviceVersion: DeviceVersion, journeyId: Option[String])(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    service.versionCheck(deviceVersion, journeyId).map {
      upgradeRequired => Ok(Json.toJson(PreFlightCheckResponse(upgradeRequired)))
    }
  }
}

@Singleton
class SandboxVersionCheckController @Inject()(implicit val ec: ExecutionContext) extends VersionCheckController {
  override def doVersionCheck(deviceVersion: DeviceVersion, journeyId: Option[String])(implicit hc: HeaderCarrier, request: Request[_]): Future[Result] = {
    val result: Result = request.headers.get("SANDBOX-CONTROL") match {
      case Some("ERROR-500") => InternalServerError
      case Some("UPGRADE-REQUIRED") => Ok(Json.toJson(PreFlightCheckResponse(true)))
      case _ => Ok(Json.toJson(PreFlightCheckResponse(false)))
    }

    Future successful result
  }
}
