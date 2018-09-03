/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.mobileversioncheck.config

import com.google.inject.name.Named
import com.google.inject.name.Names.named
import com.google.inject.{AbstractModule, Provides}
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.http.CorePost
import uk.gov.hmrc.play.config.{AppName, ServicesConfig}

class GuiceModule(environment: Environment, configuration: Configuration) extends AbstractModule with ServicesConfig {
  override protected lazy val mode: Mode = environment.mode
  override protected lazy val runModeConfiguration: Configuration = configuration

  override def configure(): Unit = {
    bind(classOf[CorePost]).to(classOf[WSHttpImpl])
    bind(classOf[String]).annotatedWith(named("customer-profile")).toInstance(baseUrl("customer-profile"))
  }

  @Provides
  @Named("appName")
  def appName: String = AppName(configuration).appName
}