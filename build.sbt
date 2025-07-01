import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "child-benefit-view-frontend"

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.6.2"
ThisBuild / scalacOptions ++= Seq(
  "-Wconf:src=routes/.*:s",
  "-Wconf:msg=unused.import&src=html/.*:s",
  "-Wconf:msg=unused.explicit.parameter&src=html/.*:s",
  "-Wconf:msg=unused.import&src=xml/.*:s",
  "-Wconf:msg=Flag.*repeatedly:s",
  "-feature",
  "-deprecation"
)

val excludedFiles = "<empty>;Reverse.*;.*utils.handlers.*;.*components.*;" +
  ".*Routes.*;.*models.viewmodels.govuk.*;.*DataRequests.*;.*HICBCCacheTestController.*;" +
  ".*LanguageSwitchController.*;.*target.*;.*PersonalInformation.*;.*BankDetails.*;.*ViewDetails.*;" +
  ".*ChangeOfBankAccountDetailsModel.*;.*ClaimantEntitlementDetails.*;.*FtnaeKickOutModel.*;.*ViewPaymentDetailsModel.*;" +
  ".*ViewProofOfEntitlementModel.*;.*VerifyBankAccountRequest.*;.*UpdateBankAccountRequest.*;.*models.audit.*;.*models.pertaxAuth.*;" +
  ".*ChildDetails.*;.*FtnaeQuestionAndAnswer.*"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq(
      "models.{NormalMode, Mode, CheckMode}",
      "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
    ),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "controllers.cob.routes._",
      "models.viewmodels.govuk.all._"
    ),
    PlayKeys.playDefaultPort               := 10650,
    ScoverageKeys.coverageExcludedFiles    := excludedFiles,
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true,
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
