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

import java.time.Instant
import com.google.inject.Singleton

import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.{JsError, JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.api.controllers.HeaderValidator
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.mobileversioncheck.domain.types.ModelTypes.JourneyId
import uk.gov.hmrc.mobileversioncheck.service.VersionCheckService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.{ExecutionContext, Future}

trait VersionCheckController extends BackendController with HeaderValidator {
  implicit def executionContext: ExecutionContext

  val logger: Logger = Logger(this.getClass)

  def versionCheck(
    journeyId: JourneyId,
    service:   String
  ): Action[JsValue] =
    validateAccept(acceptHeaderValidationRules).async(controllerComponents.parsers.json) { implicit request =>
      request.body
        .validate[DeviceVersion]
        .fold(
          errors => {
            logger.warn("Received error with service validate app version: " + errors)
            Future.successful(BadRequest(JsError.toJson(errors)))
          },
          deviceVersion => doVersionCheck(deviceVersion, journeyId, service.toLowerCase)
        )
    }

  def doVersionCheck(
    deviceVersion: DeviceVersion,
    journeyId:     JourneyId,
    service:       String
  )(implicit hc:   HeaderCarrier,
    request:       Request[_]
  ): Future[Result]
}

@Singleton
class LiveVersionCheckController @Inject() (
  val service:                   VersionCheckService,
  cc:                            ControllerComponents
)(implicit val executionContext: ExecutionContext)
    extends BackendController(cc)
    with VersionCheckController {
  override def parser: BodyParser[AnyContent] = cc.parsers.anyContent

  override def doVersionCheck(
    deviceVersion:  DeviceVersion,
    journeyId:      JourneyId,
    callingService: String
  )(implicit hc:    HeaderCarrier,
    request:        Request[_]
  ): Future[Result] =
    for {
      upgradeRequired <- service.versionCheck(deviceVersion, journeyId, callingService)
      appState        <- service.appState(callingService, deviceVersion)
    } yield {
      Ok(Json.toJson(PreFlightCheckResponse(upgradeRequired, appState)))
    }
}

@Singleton
class SandboxVersionCheckController @Inject() (
  cc:                            ControllerComponents
)(implicit val executionContext: ExecutionContext)
    extends BackendController(cc)
    with VersionCheckController {
  override def parser: BodyParser[AnyContent] = cc.parsers.anyContent

  override def doVersionCheck(
    deviceVersion:  DeviceVersion,
    journeyId:      JourneyId,
    callingService: String
  )(implicit hc:    HeaderCarrier,
    request:        Request[_]
  ): Future[Result] = {

    val result: Result = (callingService, request.headers.get("SANDBOX-CONTROL")) match {
      case (_, Some("ERROR-500")) => InternalServerError
      case ("rds", Some("UPGRADE-REQUIRED")) =>
        Ok(
          Json.toJson(
            PreFlightCheckResponse(
              upgradeRequired = true,
              Some(AppState(ACTIVE, None))
            )
          )
        )
      case (_, Some("UPGRADE-REQUIRED")) =>
        Ok(
          Json.toJson(
            PreFlightCheckResponse(upgradeRequired = true, None)
          )
        )
      case ("rds", Some("INACTIVE-APPSTATE")) =>
        Ok(
          Json.toJson(
            PreFlightCheckResponse(
              upgradeRequired = false,
              Some(AppState(INACTIVE, Some(Instant.parse("2019-11-01T00:00:00Z"))))
            )
          )
        )
      case ("ngc", Some("INACTIVE-APPSTATE"))  => InternalServerError
      case ("ngc", Some("SHUTTERED-APPSTATE")) => InternalServerError
      case ("rds", Some("SHUTTERED-APPSTATE")) =>
        Ok(
          Json.toJson(
            PreFlightCheckResponse(
              upgradeRequired = false,
              Some(AppState(SHUTTERED, Some(Instant.parse("2020-01-01T00:00:00Z"))))
            )
          )
        )
      case _ =>
        Ok(
          Json.toJson(
            PreFlightCheckResponse(
              upgradeRequired = false,
              Some(AppState(ACTIVE, None))
            )
          )
        )
    }

    Future successful result
  }
}
