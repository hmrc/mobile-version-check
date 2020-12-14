/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.mobileversioncheck.service

import java.time.Instant
import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.Json.obj
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.mobileversioncheck.domain.types.ModelTypes.JourneyId
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import uk.gov.hmrc.play.bootstrap.config.AppName

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VersionCheckService @Inject() (
  val configuration:  Configuration,
  val auditConnector: AuditConnector) {

  def versionCheck(
    deviceVersion: DeviceVersion,
    journeyId:     JourneyId,
    service:       String
  )(implicit hc:   HeaderCarrier,
    ex:            ExecutionContext
  ): Future[Boolean] = {
    sendAuditEvent("upgradeRequired", deviceVersion.os.toString)
    ValidateAppVersion.upgrade(deviceVersion, service)
  }

  private def configState(path: String): State =
    configuration.getOptional[String](path) match {
      case Some(ACTIVE.value)    => ACTIVE
      case Some(INACTIVE.value)  => INACTIVE
      case Some(SHUTTERED.value) => SHUTTERED
      case _                     => throw new IllegalStateException("Invalid State in config")
    }

  private def configEndDate(path: String): Option[Instant] = {
    val dateString = configuration.get[String](path)
    if (dateString.isEmpty) None else Some(Instant.parse(dateString))
  }

  def appState(
    service:       String,
    deviceVersion: DeviceVersion
  )(implicit hc:   HeaderCarrier,
    ex:            ExecutionContext
  ): Future[Option[AppState]] = {
    sendAuditEvent("appState", deviceVersion.os.toString)
    service match {
      case "rds" =>
        Future.successful(
          Some(
            AppState(
              state   = configState(s"$service.state"),
              endDate = configEndDate(s"$service.endDate")
            )
          )
        )
      case _ => Future.successful(None)
    }

  }

  private def sendAuditEvent(
    transactionName: String,
    deviceOs:        String
  )(implicit hc:     HeaderCarrier,
    ex:              ExecutionContext
  ): Unit = {
    auditConnector.sendEvent(
      DataEvent(
        AppName.fromConfiguration(configuration),
        "ServiceResponseSent",
        tags = Map("transactionName" -> transactionName),
        detail = Map(
          "os" -> deviceOs
        )
      )
    )
    ()
  }
}
