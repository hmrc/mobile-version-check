import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-25" % "3.1.0",
    "uk.gov.hmrc" %% "play-hmrc-api" % "2.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
        "org.scalamock" %% "scalamock" % "4.0.0" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % "3.0.0" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "info.cukes" %% "cucumber-scala" % "1.2.5" % scope,
        "info.cukes" % "cucumber-junit" % "1.2.5" % scope,
        "com.github.tomakehurst" % "wiremock" % "2.10.1" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
