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
import config.FrontendAppConfig
import controllers.actions.AuthenticatedIdentifierAction.{EnrolmentIdentifiers, EnrolmentKeys}
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, AnyContentAsEmpty, BodyParsers, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}


class IndividualAuthenticatedSpec extends SpecBase with MockitoSugar {

  import AuthenticatedIdentifierActionSpec._

  val app = applicationBuilder().build()

  val mockFrontendAppConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  val mockBodyParsersDefault = app.injector.instanceOf[BodyParsers.Default]
  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  val authenticatedIdentifierAction: AuthenticatedIdentifierAction =
    new AuthenticatedIdentifierAction(mockAuthConnector, mockFrontendAppConfig, mockBodyParsersDefault)

  "individualAuthentication" - {

    "perform the block action" - {

      "the correct enrolment exist and nino exist" - {
        val block: IdentifierRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val mtditid = "AAAAAA"
        val enrolments = Enrolments(
          Set(
            Enrolment(EnrolmentKeys.Individual,
              Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid)), "Activated"),
            Enrolment(
              EnrolmentKeys.nino,
              Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, mtditid)), "Activated")
          )
        )

        lazy val result: Future[Result] = futureResult(enrolments, block)

        "returns an OK status" in {
          status(result) mustBe OK
        }

        "returns a body of the mtditid" in {
          contentAsString(result) mustBe mtditid
        }
      }
    }

    "return a redirect" - {

      "the nino enrolment is missing" - {
        val block: IdentifierRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val enrolments = Enrolments(Set())

        lazy val result: Future[Result] = futureResult(enrolments, block)
        "returns a forbidden" in {
          status(result) mustBe SEE_OTHER
        }
      }

      "the individual enrolment is missing but there is a nino" - {
        val block: IdentifierRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val nino = "AA123456A"
        val enrolments = Enrolments(Set(Enrolment("HMRC-NI", Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino)), "Activated")))

        lazy val result: Future[Result] = futureResult(enrolments, block)
        "returns an Unauthorised" in {
          status(result) mustBe SEE_OTHER
        }
        "returns a redirect to the sign up page" in {
          redirectLocation(result) mustBe Some("/update-and-submit-income-tax-return/self-employment/error/you-need-to-sign-up")
        }
      }

    }

    "return the user to IV Uplift" - {

      "the confidence level is below minimum" - {
        val block: IdentifierRequest[AnyContent] => Future[Result] = request => Future.successful(Ok(request.user.mtditid))
        val mtditid = "1234567890"

        val enrolFn = (optNino: Option[String]) => Enrolments(Set(individualIdEnrolment(mtditid), ninoEnrolment(optNino.get)))
        lazy val result: Future[Result] = futureResult(enrolFn(Some("AA123456A")), block, ConfidenceLevel.L50)
        "has a status of 303" in {
          status(result) mustBe SEE_OTHER
        }

        "redirects to the iv url" in {
          redirectLocation(result) mustBe Some("http://localhost:9304/update-and-submit-income-tax-return/iv-uplift")
        }
      }
    }

    def futureResult(enrolments: Enrolments, block: IdentifierRequest[AnyContent] => Future[Result],
                     cl: ConfidenceLevel = ConfidenceLevel.L250): Future[Result] = {
      when(mockAuthConnector
        .authorise(any[Predicate], ArgumentMatchers.eq(Retrievals.allEnrolments and Retrievals.confidenceLevel))(
          any[HeaderCarrier], any[ExecutionContext])) thenReturn Future.successful(enrolments and cl)


      val identifierRequest = IdentifierRequest[AnyContent](fakeRequest, userId, user)
      authenticatedIdentifierAction.individualAuthentication(block, userId, AffinityGroup.Individual)(identifierRequest, emptyHeaderCarrier)
    }
  }
}

object IndividualAuthenticatedSpec {

  val individualEnrolments: Enrolments = Enrolments(Set(
    Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, "1234567890")), "Activated"),
    Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, "1234567890")), "Activated"))
  )

  val fakeRequestWithMtditid: FakeRequest[AnyContentAsEmpty.type] = FakeRequest().withSession("MTDITID" -> "1234567890")
  implicit val emptyHeaderCarrier: HeaderCarrier = HeaderCarrier()


}
