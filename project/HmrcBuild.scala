import play.core.PlayVersion
import sbt.Keys._
import sbt._

object HmrcBuild extends Build {

  import uk.gov.hmrc.SbtAutoBuildPlugin
  import uk.gov.hmrc.versioning.SbtGitVersioning

  val appName = "microservice-bootstrap"

  val appDependencies = Dependencies.compile ++ Dependencies.test

  lazy val library = Project(appName, file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      scalaVersion := "2.11.7",
      libraryDependencies ++= appDependencies,
      crossScalaVersions := Seq("2.11.7"),
      resolvers := Seq(
        Resolver.bintrayRepo("hmrc", "releases"),
        Resolver.typesafeRepo("releases"),
        Resolver.jcenterRepo
      )
    )
    .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)

}

object Dependencies {

  import play.sbt.PlayImport._

  val compile = Seq(
    filters,
    "uk.gov.hmrc" %% "play-filters" % "5.3.0",
    "uk.gov.hmrc" %% "play-graphite" % "3.0.0",
    "com.typesafe.play" %% "play" % "2.5.8",
    "de.threedimensions" %% "metrics-play" % "2.5.13"
  )

  val test = Seq(
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.pegdown" % "pegdown" % "1.5.0" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test"
  )

}


