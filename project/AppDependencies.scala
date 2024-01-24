import sbt._

object AppDependencies {

  private val bootstrapVersion  = "8.4.0"
  private val hmrcMongoVersion  = "1.2.0"
  private val enumeratumVersion = "1.7.3"

  val jacksonAndPlayExclusions = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"        %% "play-frontend-hmrc"            % "7.29.0-play-28",
    "uk.gov.hmrc"        %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-28"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-28"            % hmrcMongoVersion,
    "org.typelevel"      %% "cats-core"                     % "2.9.0",
    "com.beachape"       %% "enumeratum"                    % enumeratumVersion,
    "com.beachape"       %% "enumeratum-play-json"          % enumeratumVersion excludeAll (jacksonAndPlayExclusions *),
    "org.codehaus.janino" % "janino"                        % "3.1.11" // it's required by logback for conditional logging
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
