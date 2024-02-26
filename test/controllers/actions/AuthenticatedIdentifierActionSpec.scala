/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.actions

import base.SpecBase
import base.SpecBase.taxYear
import config.FrontendAppConfig
import controllers.actions.AuthenticatedIdentifierAction.{EnrolmentIdentifiers, EnrolmentKeys, SessionValues, User}
import models.common.TaxYear
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.SEE_OTHER
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, AnyContentAsEmpty, BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.{EmptyPredicate, Predicate}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~ => rtr}
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedIdentifierActionSpec extends SpecBase with MockitoSugar {
  import AuthenticatedIdentifierActionSpec._

  val app = applicationBuilder().build()

  val mockFrontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val mockBodyParsersDefault                   = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector         = mock[AuthConnector]

  val authenticatedIdentifierAction: AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(mockAuthConnector, mockFrontendAppConfig, mockBodyParsersDefault)

  ".invokeBlock" - {

    lazy val block: IdentifierRequest[AnyContent] => Future[Result] =
      request => Future.successful(Ok(s"mtditid: ${request.user.mtditid}${request.user.arn.fold("")(arn => " arn: " + arn)}"))

    "return a redirect" - {

      "the authorisation service returns an AuthorisationException exception" in {
        object AuthException extends AuthorisationException("Some reason")

        lazy val result = {
          mockAuthReturnException(mockAuthConnector, AuthException)
          authenticatedIdentifierAction.invokeBlock(fakeRequest, block)
        }
        status(result) mustBe SEE_OTHER
      }

      "there is no MTDITID value in session for an agent" in {
        lazy val result = {

          mockAuthAgentAffinityGroup(mockAuthConnector)
          authenticatedIdentifierAction.invokeBlock(fakeRequestWithNino, block)
        }
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some("/update-and-submit-income-tax-return/self-employment/unauthorised")
      }

    }

    "redirect to the sign in page" - {
      "the authorisation service returns a NoActiveSession exception" in {
        object NoActiveSession extends NoActiveSession("Some reason")

        lazy val result = {
          mockAuthReturnException(mockAuthConnector, NoActiveSession)
          authenticatedIdentifierAction.invokeBlock(fakeRequest, block)
        }

        status(result) mustBe SEE_OTHER
      }
    }
  }
}

object AuthenticatedIdentifierActionSpec {
  def agentEnrolment(ref: String): Enrolment =
    Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, ref)), "Activated")

  def individualIdEnrolment(id: String): Enrolment =
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, id)), "Activated")
  def ninoEnrolment(nino: String): Enrolment = Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated")

  def ninoEnrolments(nino: Option[String]): Enrolments = Enrolments(
    agentEnrolments.enrolments ++ nino.fold(Seq.empty[Enrolment])(unwrappedNino => Seq(ninoEnrolment(unwrappedNino)))
  )

  val agentEnrolments: Enrolments = Enrolments(
    Set(individualIdEnrolment("1234567890"), agentEnrolment("0987654321"))
  )

  def mockAuthInvoke(mockAuthConnector: AuthConnector): Unit = {
    val retrieval = rtr(Some(userId), Some(AffinityGroup.Agent))

    when(
      mockAuthConnector
        .authorise(ArgumentMatchers.eq(EmptyPredicate), ArgumentMatchers.eq(Retrievals.internalId and Retrievals.affinityGroup))(
          any[HeaderCarrier],
          any[ExecutionContext]))
      .thenReturn(Future.successful(retrieval))

    ()
  }

  def mockAuthAsAgent(mockAuthConnector: AuthConnector): Unit = {
    when(
      mockAuthConnector
        .authorise(any[Predicate], ArgumentMatchers.eq(Retrievals.allEnrolments))(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future
      .successful(agentEnrolments)

    mockAuthAgentAffinityGroup(mockAuthConnector)
    ()
  }

  def mockAuthAsIndividual(mockAuthConnector: AuthConnector,
                           nino: Option[String],
                           cl: ConfidenceLevel = ConfidenceLevel.L250,
                           enrolFn: Option[String] => Enrolments = ninoEnrolments): Unit = {
    when(
      mockAuthConnector
        .authorise(any[Predicate], ArgumentMatchers.eq(Retrievals.allEnrolments and Retrievals.confidenceLevel))(
          any[HeaderCarrier],
          any[ExecutionContext])) thenReturn Future.successful(enrolFn(nino) and cl)

    mockAuthIndividualAffinityGroup(mockAuthConnector)
  }

  def mockAuthAgentAffinityGroup(mockAuthConnector: AuthConnector): Unit = {
    when(
      mockAuthConnector
        .authorise(any[Predicate], ArgumentMatchers.eq(Retrievals.affinityGroup))(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future
      .successful(Some(AffinityGroup.Agent))
    ()
  }

  def mockAuthIndividualAffinityGroup(mockAuthConnector: AuthConnector): Unit = {
    when(
      mockAuthConnector
        .authorise(any[Predicate], ArgumentMatchers.eq(Retrievals.affinityGroup))(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future
      .successful(Some(AffinityGroup.Individual))
    ()
  }

  def mockAuthReturnException(mockAuthConnector: AuthConnector, exception: Exception): Unit = {
    when(
      mockAuthConnector
        .authorise(any[Predicate], any[Retrieval[_]])(any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.failed(exception)
    ()
  }

  val sessionId: String                                         = "eb3158c2-0aff-4ce8-8d1b-f2208ace52fe"
  implicit val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withHeaders("mtditid" -> "1234567890")

  private val validTaxYearList: Seq[TaxYear] = {
    val year = taxYear.endYear
    Seq(TaxYear(year - 2), TaxYear(year - 1), taxYear)
  }

  val fakeRequestWithMtditidAndNino: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withSession(
      SessionValues.CLIENT_MTDITID  -> "1234567890",
      SessionValues.CLIENT_NINO     -> "AA123456A",
      SessionValues.TAX_YEAR        -> taxYear.toString,
      SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
    )
    .withHeaders("X-Session-ID" -> sessionId)

  val fakeRequestWithNino: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession(
    SessionValues.CLIENT_NINO     -> "AA123456A",
    SessionValues.VALID_TAX_YEARS -> validTaxYearList.mkString(",")
  )

  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()

  val user   = User(mtditid = "1234567890", arn = None, nino = "AA112233A", AffinityGroup.Individual.toString)
  val userId = "987987987"
}
