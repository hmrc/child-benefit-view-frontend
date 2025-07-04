import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.13.0"
  private val hmrcMongoVersion = "2.6.0"
  private val playVersion      = "play-30"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    play.sbt.PlayImport.caffeine,
    "org.typelevel"     %% "cats-core"                                  % "2.13.0",
    "uk.gov.hmrc"       %% s"sca-wrapper-$playVersion"                  % "2.15.0",
    "uk.gov.hmrc"       %% s"mongo-feature-toggles-client-$playVersion" % "1.10.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion,
    "org.scalatestplus" %% "scalacheck-1-18"               % "3.2.19.0",
    "org.jsoup"          % "jsoup"                         % "1.20.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
