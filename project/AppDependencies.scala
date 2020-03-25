import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion     = "1.3.0"
  private val playHmrcVersion          = "4.1.0-play-26"
  private val scalatestPlusPlayVersion = "3.1.2"

  private val scalaMockVersion       = "4.1.0"
  private val pegdownVersion         = "1.6.0"
  private val integrationTestVersion = "0.10.0-play-26"
  private val cucumberJunitVersion   = "1.2.5"
  private val cucumberScalaVersion   = "1.2.5"
  private val wiremockVersion        = "2.21.0"
  private val refinedVersion         = "0.9.4"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-hmrc-api"     % playHmrcVersion,
    "eu.timepit"  %% "refined"           % refinedVersion
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] =
      new TestDependencies {
        override lazy val test: Seq[ModuleID] = Seq(
          "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
          "org.scalamock"          %% "scalamock"          % scalaMockVersion         % scope,
          "org.pegdown"            % "pegdown"             % pegdownVersion           % scope
        )
      }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = Seq(
          "org.scalatestplus.play" %% "scalatestplus-play"       % scalatestPlusPlayVersion % scope,
          "org.pegdown"            % "pegdown"                   % pegdownVersion           % scope,
          "uk.gov.hmrc"            %% "service-integration-test" % integrationTestVersion   % scope,
          "com.typesafe.play"      %% "play-test"                % PlayVersion.current      % scope,
          "info.cukes"             %% "cucumber-scala"           % cucumberJunitVersion     % scope,
          "info.cukes"             % "cucumber-junit"            % cucumberScalaVersion     % scope,
          "com.github.tomakehurst" % "wiremock"                  % wiremockVersion          % scope
        )
      }.test

    // Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
    // compatible with wiremock, so we need to pin the jetty stuff to the older version.
    // see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
    val jettyVersion = "9.2.13.v20150730"
    def overrides(): Seq[ModuleID] = Seq(
      "org.eclipse.jetty"           % "jetty-server"       % jettyVersion,
      "org.eclipse.jetty"           % "jetty-servlet"      % jettyVersion,
      "org.eclipse.jetty"           % "jetty-security"     % jettyVersion,
      "org.eclipse.jetty"           % "jetty-servlets"     % jettyVersion,
      "org.eclipse.jetty"           % "jetty-continuation" % jettyVersion,
      "org.eclipse.jetty"           % "jetty-webapp"       % jettyVersion,
      "org.eclipse.jetty"           % "jetty-xml"          % jettyVersion,
      "org.eclipse.jetty"           % "jetty-client"       % jettyVersion,
      "org.eclipse.jetty"           % "jetty-http"         % jettyVersion,
      "org.eclipse.jetty"           % "jetty-io"           % jettyVersion,
      "org.eclipse.jetty"           % "jetty-util"         % jettyVersion,
      "org.eclipse.jetty.websocket" % "websocket-api"      % jettyVersion,
      "org.eclipse.jetty.websocket" % "websocket-common"   % jettyVersion,
      "org.eclipse.jetty.websocket" % "websocket-client"   % jettyVersion
    )
  }

  def apply():     Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
  def overrides(): Seq[ModuleID] = IntegrationTest.overrides()
}
