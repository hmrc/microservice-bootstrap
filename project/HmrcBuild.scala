import sbt.Keys._
import sbt._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning


object HmrcBuild extends Build {

  import BuildDependencies._
  import uk.gov.hmrc.DefaultBuildSettings._

  val appName = "microservice-bootstrap"

  lazy val MicroserviceBootstrap = (project in file("."))
    .enablePlugins(SbtAutoBuildPlugin, SbtGitVersioning)
    .settings(
      name := appName,
      targetJvm := "jvm-1.7",
      libraryDependencies ++= AppDependencies.apply(),
      Developers()
    )
}

private object AppDependencies {

  import play.core.PlayVersion

  val httpVerbsVersion = "1.7.0"

  val compile = Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current,
    "uk.gov.hmrc" %% "play-config" % "1.0.0",
    "uk.gov.hmrc" %% "http-verbs" % "1.7.0",
    "uk.gov.hmrc" %% "play-filters" % "1.2.0",
    "uk.gov.hmrc" %% "play-graphite" % "1.0.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope,
        "uk.gov.hmrc" %% "hmrctest" % "1.0.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}

object Developers {

  def apply() = developers := List[Developer]()
}
