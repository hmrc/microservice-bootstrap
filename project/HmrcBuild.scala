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
  val playAuthorisationVersion = "0.14.0"

  val compile = Seq(
    "com.typesafe.play" %% "play" % PlayVersion.current,

//    "com.kenshoo" %% "metrics-play" % "2.3.0_0.1.8",
    "uk.gov.hmrc" %% "play-config" % "1.0.0",
    "uk.gov.hmrc" %% "http-verbs" % httpVerbsVersion,
    "uk.gov.hmrc" %% "play-authorisation" % playAuthorisationVersion,
    "uk.gov.hmrc" %% "play-filters" % "1.2.0",
    "uk.gov.hmrc" %% "play-graphite" % "1.0.0",
    "uk.gov.hmrc" %% "play-json-logger" % "1.0.0"
//    "commons-io" % "commons-io" % "2.4",
//    "net.ceedubs" %% "ficus" % "1.1.1",
//    "uk.gov.hmrc" %% "time" % "1.1.0",
//    "uk.gov.hmrc" %% "domain" % "2.6.0",
//    "uk.gov.hmrc" % "secure-utils" % "5.1.0"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = ???
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
//        "commons-codec" % "commons-codec" % "1.8.0" % scope,
        "org.scalatest" %% "scalatest" % "2.2.2" % scope,
        "org.pegdown" % "pegdown" % "1.4.2" % scope,
//        "com.github.tomakehurst" % "wiremock" % "1.48" % scope,
//        "uk.gov.hmrc" %% "http-verbs" % httpVerbsVersion % scope classifier "tests",
        "uk.gov.hmrc" %% "hmrctest" % "1.0.0" % scope
      )
    }.test
  }

  def apply() = compile ++ Test()
}

object Developers {

  def apply() = developers := List[Developer]()
}
