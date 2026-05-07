import sbt.*

object AppDependencies {

  private val bootstrapVersion  = "10.7.0"
  private val hmrcMongoVersion  = "2.12.0"
  private val enumeratumVersion = "1.9.6"

  val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"        %% "play-frontend-hmrc-play-30"            % "12.32.0",
    "uk.gov.hmrc"        %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.typelevel"      %% "cats-core"                             % "2.13.0",
    "com.beachape"       %% "enumeratum"                            % enumeratumVersion,
    "com.beachape"       %% "enumeratum-play-json"                  % enumeratumVersion excludeAll (jacksonAndPlayExclusions*),
    "org.codehaus.janino" % "janino"                                % "3.1.12" // it's required by logback for conditional logging
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo"   %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "com.vladsch.flexmark" % "flexmark-all"            % "0.64.8",
    "org.scalatest"       %% "scalatest"               % "3.2.19",
    "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0", // ScalaCheck 1.18: Gen.containerOf / Shrink behaviour changed from 1.15 — run property tests in isolation if generators produce unexpected data
    "org.typelevel"     %% "cats-core"       % "2.13.0",
    "org.scalacheck"    %% "scalacheck"      % "1.18.1",
    "org.mockito"       %% "mockito-scala"   % "2.2.1"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
