import sbt._

object AppDependencies {

  private val bootstrapVersion  = "7.15.0"
  private val hmrcMongoVersion  = "1.2.0"
  private val enumeratumVersion = "1.7.3"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "7.9.0-play-28",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "com.beachape"      %% "enumeratum"                    % enumeratumVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-28"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-28" % hmrcMongoVersion,
    "org.scalatest"       %% "scalatest"               % "3.2.10",
    "org.scalatestplus"   %% "scalacheck-1-15"         % "3.2.10.0",
    "org.scalatestplus"   %% "mockito-3-4"             % "3.2.10.0",
    "org.typelevel"       %% "cats-core"               % "2.9.0",
    "org.mockito"         %% "mockito-scala"           % "1.16.42",
    "org.scalacheck"      %% "scalacheck"              % "1.15.4",
    "org.pegdown"          % "pegdown"                 % "1.6.0",
    "org.jsoup"            % "jsoup"                   % "1.14.3",
    "com.vladsch.flexmark" % "flexmark-all"            % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
