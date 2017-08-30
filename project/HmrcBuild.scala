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
      scalaVersion := "2.11.11",
      libraryDependencies ++= appDependencies,
      crossScalaVersions := Seq("2.11.11"),
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
    "uk.gov.hmrc" %% "http-verbs" % "7.1.0",
    "uk.gov.hmrc" %% "http-verbs-play-25" % "0.7.0",
    "uk.gov.hmrc" %% "play-auditing" % "3.2.0",
    "uk.gov.hmrc" %% "play-graphite" % "3.2.0",
    "uk.gov.hmrc" %% "play-config" % "4.3.0",
    "uk.gov.hmrc" %% "play-authorisation" % "5.0.0",
    "uk.gov.hmrc" %% "play-health" % "2.1.0",
    "ch.qos.logback" % "logback-core" % "1.1.7",
    "uk.gov.hmrc" %% "logback-json-logger" % "3.1.0",
    "com.typesafe.play" %% "play" % "2.5.12",
    "de.threedimensions" %% "metrics-play" % "2.5.13",
    "ch.qos.logback" % "logback-core" % "1.1.7"
  )

  val test = Seq(
    "com.typesafe.play" %% "play-test" % PlayVersion.current % "test",
    "com.typesafe.play" %% "play-specs2" % PlayVersion.current % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "org.pegdown" % "pegdown" % "1.5.0" % "test",
    "org.mockito" % "mockito-all" % "1.9.5" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % "test",
    "uk.gov.hmrc" %% "hmrctest" % "2.3.0" % "test"
  )

}


