import sbt.*

object AppDependencies {
  import play.core.PlayVersion

  private val bootstrapVersion = "8.5.0"
  private val hmrcMongoVersion = "1.8.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"         % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                  % "2.10.0",
    "uk.gov.hmrc"       %% "sca-wrapper-play-30"        % "1.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"          %% "scalatest"               % "3.2.18",
    "org.scalatestplus"      %% "mockito-4-6"             % "3.2.15.0",
    "org.scalatestplus.play" %% "scalatestplus-play"      % "7.0.1",
    "org.scalatestplus"      %% "scalacheck-1-17"         % "3.2.18.0",
    "org.pegdown"             % "pegdown"                 % "1.6.0",
    "org.jsoup"               % "jsoup"                   % "1.17.2"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
