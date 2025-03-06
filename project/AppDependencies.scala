import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.9.0"
  private val hmrcMongoVersion = "2.5.0"

  val jacksonAndPlayExclusions: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "com.fasterxml.jackson.core"),
    ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
    ExclusionRule(organization = "com.fasterxml.jackson.module"),
    ExclusionRule(organization = "com.fasterxml.jackson.core:jackson-annotations"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"        %% "play-frontend-hmrc-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"        %% "play-conditional-form-mapping-play-30" % "3.2.0",
    "uk.gov.hmrc"        %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo"  %% "hmrc-mongo-play-30"                    % hmrcMongoVersion,
    "org.typelevel"      %% "cats-core"                             % "2.12.0",
    "com.beachape"       %% "enumeratum"                            % "1.7.4",
    "com.beachape"       %% "enumeratum-play-json"                  % "1.8.1" excludeAll (jacksonAndPlayExclusions *),
    "org.codehaus.janino" % "janino"                                % "3.1.12" // it's required by logback for conditional logging
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "org.scalatest"     %% "scalatest"               % "3.2.19",
    "org.scalatestplus" %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus" %% "mockito-3-4"             % "3.2.10.0",
    "org.typelevel"     %% "cats-core"               % "2.12.0",
    "org.scalacheck"    %% "scalacheck"              % "1.18.0",
    "org.pegdown"        % "pegdown"                 % "1.6.0",
    "org.mockito"       %% "mockito-scala"           % "1.17.37",
    "org.scalamock"     %% "scalamock"               % "5.2.0",
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
