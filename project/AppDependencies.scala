import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % "0.41.0",
    "uk.gov.hmrc" %% "play-hmrc-api"     % "3.4.0-play-26"
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  private val scalatestPlusPlayVersion = "3.1.2"

  object Test {
    def apply(): Seq[ModuleID] =
      new TestDependencies {
        override lazy val test: Seq[ModuleID] = Seq(
          "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
          "org.scalamock"          %% "scalamock"          % "4.1.0"                  % scope,
          "org.pegdown"            % "pegdown"             % "1.6.0"                  % scope
        )
      }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = Seq(
          "org.scalatestplus.play" %% "scalatestplus-play"       % scalatestPlusPlayVersion % scope,
          "org.pegdown"            % "pegdown"                   % "1.6.0"                  % scope,
          "uk.gov.hmrc"            %% "service-integration-test" % "0.4.0-play-26"          % scope,
          "com.typesafe.play"      %% "play-test"                % PlayVersion.current      % scope,
          "info.cukes"             %% "cucumber-scala"           % "1.2.5"                  % scope,
          "info.cukes"             % "cucumber-junit"            % "1.2.5"                  % scope,
          "com.github.tomakehurst" % "wiremock"                  % "2.20.0"                 % scope
        )
      }.test

    // Transitive dependencies in scalatest/scalatestplusplay drag in a newer version of jetty that is not
    // compatible with wiremock, so we need to pin the jetty stuff to the older version.
    // see https://groups.google.com/forum/#!topic/play-framework/HAIM1ukUCnI
    val jettyVersion = "9.2.13.v20150730"
    def overrides(): Set[ModuleID] = Set(
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
  def overrides(): Set[ModuleID] = IntegrationTest.overrides()
}
