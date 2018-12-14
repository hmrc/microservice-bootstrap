  val appName = "microservice-bootstrap"

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory)
    .settings(
      majorVersion := 10,
      scalaVersion := "2.11.12",
      makePublicallyAvailableOnBintray := true,
      libraryDependencies ++= LibDependencies.compile ++ LibDependencies.test,
      crossScalaVersions := Seq("2.11.12"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases"),
        Resolver.jcenterRepo
      )
    )
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

