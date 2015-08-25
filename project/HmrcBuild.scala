import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc.DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appName = "microservice-bootstrap"

  val appDependencies = Dependencies.compile ++ Dependencies.test


  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      targetJvm := "jvm-1.7",
      libraryDependencies ++= appDependencies,
      crossScalaVersions := Seq("2.11.7"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases")
      )
    )
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

}

object Dependencies {

  import play.PlayImport._
  import play.core.PlayVersion

  val compile = Seq(
    filters,
    "uk.gov.hmrc" %% "play-authorisation" % "0.15.0",
    "uk.gov.hmrc" %% "play-filters" % "1.3.0",
    "uk.gov.hmrc" %% "play-graphite" % "1.3.0",
    "com.typesafe.play" %% "play" % PlayVersion.current,
    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8"
  )

  val test = Seq(
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.scalatest" %% "scalatest" % "2.2.2" % "test",
    "org.pegdown" % "pegdown" % "1.4.2" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test"
  )

}


