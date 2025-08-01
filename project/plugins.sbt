resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(
  Resolver.ivyStylePatterns
)
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"

addSbtPlugin("uk.gov.hmrc"       % "sbt-auto-build"     % "3.24.0")
addSbtPlugin("uk.gov.hmrc"       % "sbt-distributables" % "2.6.0")
addSbtPlugin("org.playframework" % "sbt-plugin"         % "3.0.7")
addSbtPlugin("org.scoverage"     % "sbt-scoverage"      % "2.3.1")
addSbtPlugin("com.timushev.sbt"  % "sbt-updates"        % "0.3.4")
