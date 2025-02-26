import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.8.0"
  private val hmrcMongoVersion = "2.4.0"
  private val playVersion      = "play-30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    play.sbt.PlayImport.caffeine,
    "org.typelevel"     %% "cats-core"                                  % "2.13.0",
    "uk.gov.hmrc"       %% s"sca-wrapper-$playVersion"                  % "2.1.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-$playVersion" % "1.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatestplus"      %% "scalacheck-1-18"               % "3.2.19.0",
    "org.pegdown"             % "pegdown"                       % "1.6.0",
    "org.jsoup"               % "jsoup"                         % "1.18.3"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
