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

package uk.gov.hmrc.mobileversioncheck.domain

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ValueReader
import uk.gov.hmrc.mobileversioncheck.domain.NativeOS.{Android, iOS}

import scala.concurrent.Future

case class NativeVersion(
  ios:     VersionRange,
  android: VersionRange)

trait ValidateAppVersion {
  def config: Config

  def upgrade(
    deviceVersion: DeviceVersion,
    service:       String
  ): Future[Boolean] = {

    val appVersion: NativeVersion = service match {
      case "rds" => {
        implicit val nativeVersionReader: ValueReader[NativeVersion] = ValueReader.relative { _ =>
          NativeVersion(
            VersionRange(config.as[String]("approvedAppVersions.rds.ios")),
            VersionRange(config.as[String]("approvedAppVersions.rds.android"))
          )
        }
        config.as[NativeVersion]("approvedAppVersions.rds")
      }

      case "ngc" => {
        implicit val nativeVersionReader: ValueReader[NativeVersion] = ValueReader.relative { _ =>
          NativeVersion(
            VersionRange(config.as[String]("approvedAppVersions.ngc.ios")),
            VersionRange(config.as[String]("approvedAppVersions.ngc.android"))
          )
        }
        config.as[NativeVersion]("approvedAppVersions.ngc")
      }

      case _ => throw new IllegalStateException
    }

    val outsideValidRange = deviceVersion.os match {
      case `iOS`   => !appVersion.ios.includes(Version(deviceVersion.version))
      case Android => !appVersion.android.includes(Version(deviceVersion.version))
    }
    Future.successful(outsideValidRange)
  }
}

object ValidateAppVersion extends ValidateAppVersion {

  import com.typesafe.config.{Config, ConfigFactory}

  lazy val config: Config = ConfigFactory.load()
}
