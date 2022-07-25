import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val payApiVersion = "1.89.0"

  val compile = Seq(
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"       % "5.7.0",
    "uk.gov.hmrc"       %% "play-frontend-govuk"              % "0.80.0-play-28",
    "com.beachape"      %% "enumeratum-play"                  % "1.7.0"
  )

  val test = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"  % "5.6.0"  % "test",
    "org.scalatest"          %% "scalatest"               % "3.2.9"  % "test",
    "com.vladsch.flexmark"   %  "flexmark-all"            % "0.36.8" % "test",
    "com.typesafe.play"      %% "play-test"               % current  % "test",
    "org.pegdown"            %  "pegdown"                 % "1.6.0"  % "test",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "5.1.0"  % "test",
    "com.github.tomakehurst" %  "wiremock-jre8"           % "2.26.0" % "test"
  )
}
