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

package uk.gov.hmrc.mobileversioncheck.service

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mobileversioncheck.domain._
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.bootstrap.config.AppName
import uk.gov.hmrc.service.Auditor

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class VersionCheckService @Inject()(val configuration: Configuration, val auditConnector: AuditConnector) extends Auditor {

  def versionCheck(deviceVersion: DeviceVersion, journeyId: String, service: String)(
    implicit hc:                  HeaderCarrier,
    ex:                           ExecutionContext): Future[Boolean] =
    withAudit("upgradeRequired", Map("os" -> deviceVersion.os.toString)) {
      ValidateAppVersion.upgrade(deviceVersion, service)
    }

  private def configState(path: String): State =
    configuration.getOptional[String](path) match {
      case Some(ACTIVE.value)    => ACTIVE
      case Some(INACTIVE.value)  => INACTIVE
      case Some(SHUTTERED.value) => SHUTTERED
      case _                     => throw new IllegalStateException("Invalid State in config")
    }

  private def configEndDate(path: String): Option[LocalDateTime] = {
    val dateString = configuration.get[String](path)
    if (dateString.isEmpty) None else Some(LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
  }

  def appState(service: String, deviceVersion: DeviceVersion)(implicit hc: HeaderCarrier, ex: ExecutionContext): Future[AppState] =
    withAudit("appState", Map("os" -> deviceVersion.os.toString)) {
      Future.successful(
        AppState(
          state   = configState(s"$service.state"),
          endDate = configEndDate(s"$service.endDate")
        ))

    }

  override def appName: String = AppName.fromConfiguration(configuration)
}
