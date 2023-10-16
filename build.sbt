import play.sbt.PlayImport.PlayKeys.playDefaultPort
import sbt.Tests.{Group, SubProcess}
import uk.gov.hmrc.DefaultBuildSettings.addTestReportOption

val appName = "mobile-version-check"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin, ScoverageSbtPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.12.15",
    playDefaultPort := 8244,
    libraryDependencies ++= AppDependencies(),
    update / evictionWarningOptions := EvictionWarningOptions.default
      .withWarnScalaVersionEviction(warnScalaVersionEviction = false),
    coverageMinimumStmtTotal := 90,
    coverageFailOnMinimum := true,
    coverageHighlighting := true,
    coverageExcludedPackages := "<empty>;.*Routes.*;app.*;.*prod;.*definition;.*testOnlyDoNotUseInAppConf;.*com.kenshoo.*;.*javascript.*;.*BuildInfo;.*Reverse.*"
  )
  .settings(
    routesImport ++= Seq("uk.gov.hmrc.mobileversioncheck.domain.types._",
                         "uk.gov.hmrc.mobileversioncheck.domain.types.ModelTypes._")
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory)(base => Seq(base / "it")).value,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
    IntegrationTest / parallelExecution := false,
    addTestReportOption(IntegrationTest, "int-test-reports")
  )
  .settings(
    resolvers += Resolver.jcenterRepo
  )

def oneForkedJvmPerTest(tests: Seq[TestDefinition]): Seq[Group] =
  tests map { test =>
    Group(test.name, Seq(test), SubProcess(ForkOptions().withRunJVMOptions(Vector(s"-Dtest.name=${test.name}"))))
  }
