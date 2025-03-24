import com.typesafe.sbt.uglify.Import.uglifyOps
import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "income-tax-self-employment-frontend"
ThisBuild / majorVersion      := 0
ThisBuild / scalaVersion      := "2.13.16"
ThisBuild / scalafmtOnCompile := true
ThisBuild / useSuperShell     := false
Global / excludeLintKeys += majorVersion // suppress 'ThisBuild / majorVersion key unused' warning

val additionalScalacOptions = if (sys.props.getOrElse("PLAY_ENV", "") == "CI") Seq("-Xfatal-warnings") else Seq()

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) // Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inConfig(Test)(testSettings) *)
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings) *)
  .settings(
    PlayKeys.playDefaultPort := 10901,
    libraryDependencies ++= AppDependencies(),
    RoutesKeys.routesImport ++= Seq("models._", "models.common._", "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"),
    TwirlKeys.templateImports ++= Seq(
      "config.FrontendAppConfig",
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.hmrcfrontend.views.config._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._",
      "viewmodels.govuk.all._"
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-unchecked",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:params",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Ywarn-value-discard",
      "-rootdir",
      baseDirectory.value.getCanonicalPath,
      "-Wconf:src=target/.*:s,src=routes/.*:s" // suppress warnings in generated routes files
    ) ++ additionalScalacOptions,
    retrieveManaged := true,
    resolvers ++= Seq(Resolver.jcenterRepo),
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(
          Seq(
            "javascripts/app.js"
          ))
    ),
    // prevent removal of unused code which generates warning errors due to use of third-party libs
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages        := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    Assets / pipelineStages := Seq(concat, uglify),
    uglifyOps               := UglifyOps.singleFile, // no source map
    // only compress files generated by concat
    uglify / includeFilter := GlobFilter("application.js")
  )
  .settings(CodeCoverageSettings.settings *)

lazy val testSettings: Seq[Def.Setting[_]] = Seq(
  fork := false,
  unmanagedSourceDirectories += baseDirectory.value / "test-utils"
)

lazy val itSettings = Defaults.testSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it",
    baseDirectory.value / "test-utils"
  ),
  unmanagedResourceDirectories := Seq(
    baseDirectory.value / "it" / "resources"
  ),
  parallelExecution := false,
  fork              := true
)
