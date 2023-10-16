import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion     = "5.24.0"
  private val playHmrcVersion          = "7.0.0-play-28"
  private val scalatestPlusPlayVersion = "4.0.3"

  private val scalaMockVersion       = "4.1.0"
  private val pegdownVersion         = "1.6.0"
  private val integrationTestVersion = "1.2.0-play-28"
  private val wiremockVersion        = "2.27.2"
  private val refinedVersion         = "0.9.19"
  private val flexmarkAllVersion     = "0.35.10"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-hmrc-api"             % playHmrcVersion,
    "eu.timepit"  %% "refined"                   % refinedVersion
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  private def testCommon(scope: String) = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28" % bootstrapPlayVersion     % scope,
    "org.scalatestplus.play" %% "scalatestplus-play"     % scalatestPlusPlayVersion % scope,
    "org.pegdown"            % "pegdown"                 % pegdownVersion           % scope,
    "com.vladsch.flexmark"   % "flexmark-all"            % flexmarkAllVersion       % scope
  )

  object Test {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "org.scalamock" %% "scalamock" % scalaMockVersion % scope
          )
      }.test
  }

  object IntegrationTest {

    def apply(): Seq[ModuleID] =
      new TestDependencies {

        override lazy val scope: String = "it"

        override lazy val test: Seq[ModuleID] = testCommon(scope) ++ Seq(
            "com.typesafe.play"      %% "play-test" % PlayVersion.current % scope,
            "com.github.tomakehurst" % "wiremock"   % wiremockVersion     % scope
          )
      }.test

  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
