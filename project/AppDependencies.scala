import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.6.0"
  private val hmrcMongoVersion = "2.2.0"
  private val playVersion      = "play-30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    play.sbt.PlayImport.caffeine,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-$playVersion"                   % hmrcMongoVersion,
    "org.typelevel"     %% "cats-core"                                  % "2.12.0",
    "uk.gov.hmrc"       %% s"sca-wrapper-$playVersion"                  % "1.12.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-$playVersion" % "1.6.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatest"          %% "scalatest"                     % "3.2.19",
    "org.scalatestplus"      %% "mockito-4-6"                   % "3.2.15.0",
    "org.scalatestplus.play" %% "scalatestplus-play"            % "7.0.1",
    "org.scalatestplus"      %% "scalacheck-1-17"               % "3.2.18.0",
    "org.pegdown"             % "pegdown"                       % "1.6.0",
    "org.jsoup"               % "jsoup"                         % "1.18.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
