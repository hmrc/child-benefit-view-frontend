import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.5.0"
  private val hmrcMongoVersion = "2.3.0"
  private val playVersion      = "play-30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    play.sbt.PlayImport.caffeine,
    "org.typelevel"     %% "cats-core"                                  % "2.12.0",
    "uk.gov.hmrc"       %% s"sca-wrapper-$playVersion"                  % "2.1.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-$playVersion" % "1.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatestplus"      %% "scalacheck-1-17"               % "3.2.18.0",
    "org.pegdown"             % "pegdown"                       % "1.6.0",
    "org.jsoup"               % "jsoup"                         % "1.18.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
