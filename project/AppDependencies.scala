import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val bootstrapPlayVersion = "9.16.0"
  private val playHmrcVersion      = "8.0.0"
  private val scalaMockVersion     = "5.2.0"
  private val refinedVersion       = "0.11.2"
  private val ficusVersion         = "1.4.3"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "uk.gov.hmrc" %% "play-hmrc-api-play-30"     % playHmrcVersion,
    "eu.timepit"  %% "refined"                   % refinedVersion
  )

  trait TestDependencies {
    lazy val scope: String        = "test"
    lazy val test:  Seq[ModuleID] = ???
  }

  private def testCommon(scope: String) = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope
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

        override lazy val test: Seq[ModuleID] = testCommon(scope)
      }.test

  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
