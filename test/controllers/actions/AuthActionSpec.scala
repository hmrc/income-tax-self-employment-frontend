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
import common.{DelegatedAuthRules, EnrolmentIdentifiers, EnrolmentKeys}
import config.MockAppConfig
import connectors.MockAuthConnector
import controllers.standard.routes
import mocks.MockErrorHandler
import models.errors.ServiceError.MissingAgentClientDetails
import play.api.Application
import play.api.mvc.Results.InternalServerError
import play.api.mvc._
import play.api.test.Helpers._
import services.mocks.MockSessionDataService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.~
import utils.EnrolmentHelper

import scala.concurrent.Future

class AuthActionSpec extends SpecBase with MockAppConfig with MockSessionDataService with MockAuthConnector with MockErrorHandler {

  trait Fixture {

    MockAppConfig.signInUrl("/sign-in")
    MockAppConfig.viewAndChangeEnterUtrUrl("/enter-utr")
    MockAppConfig.incomeTaxSubmissionIvRedirect("/iv-uplift")

    lazy val application: Application         = applicationBuilder(userAnswers = None).build()
    lazy val bodyParsers: BodyParsers.Default = application.injector.instanceOf[BodyParsers.Default]

    lazy val authAction = new AuthenticatedIdentifierAction(
      mockAuthConnector,
      mockErrorHandler,
      mockAppConfig,
      mockSessionDataService,
      bodyParsers
    )

    class Harness(authAction: IdentifierAction) {
      def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
    }

    lazy val controller = new Harness(authAction)
  }

  "Auth Action" - {

    "when the user is an Individual" - {

      "authorised with a satisfactory confidence level" - {

        "has a NINO and MTDITID enrolment" - {

          "must return OK" in new Fixture {

            val enrolments: Enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid.value)), "Activated"),
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino.value)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe OK
          }
        }

        "has a missing NINO" - {

          "must return SEE_OTHER and redirect to login" in new Fixture {

            val enrolments: Enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid.value)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(mockAppConfig.signInUrl)
          }
        }

        "has a missing MTDITID" - {

          "must return SEE_OTHER and redirect to individual auth error page" in new Fixture {

            val enrolments: Enrolments = Enrolments(
              Set(
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino.value)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Individual)))
              )
              .once()

            MockAuthConnector
              .authorise(EmptyPredicate)(
                Future.successful(new ~(enrolments, ConfidenceLevel.L250))
              )
              .once()

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe Some(controllers.authorisationErrors.routes.IndividualAuthErrorController.onPageLoad.url)
          }
        }
      }

      "unauthorised with an insufficient confidence level" - {

        "must return SEE_OTHER and redirect to IV Uplift journey" in new Fixture {

          val enrolments: Enrolments = Enrolments(
            Set(
              Enrolment(EnrolmentKeys.Individual, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid.value)), "Activated"),
              Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino.value)), "Activated")
            ))

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Individual)))
            )
            .once()

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new ~(enrolments, ConfidenceLevel.L200))
            )
            .once()

          val result: Future[Result] = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.incomeTaxSubmissionIvRedirect)
        }
      }
    }

    "when user is an Agent" - {

      "when a clientID and NINO are returned from SessionData service" - {

        "Authorised as a Primary Agent" - {

          "must return OK" in new Fixture {

            MockSessionDataService.getSessionData(sessionId)(sessionData)

            val enrolments: Enrolments = Enrolments(
              Set(
                Enrolment(
                  key = EnrolmentKeys.Individual,
                  identifiers = Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid.value)),
                  state = "Activated",
                  delegatedAuthRule = Some(DelegatedAuthRules.agentDelegatedAuthRule)
                ),
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino.value)), "Activated"),
                Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
              ))

            MockAuthConnector
              .authorise(EmptyPredicate)(Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent))))

            MockAuthConnector
              .authorise(EnrolmentHelper.agentAuthPredicate(mtditid.value))(Future.successful(enrolments))

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe OK
          }
        }

        "an unexpected error occurs during primary auth call" - {
          "must return INTERNAL_SERVER_ERROR" in new Fixture {

            MockSessionDataService.getSessionData(sessionId)(sessionData)

            MockAuthConnector
              .authorise(EmptyPredicate)(Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent))))

            MockAuthConnector
              .authorise(EnrolmentHelper.agentAuthPredicate(mtditid.value))(Future.failed(InsufficientEnrolments("Error")))

            mockInternalServerError(InternalServerError("An unexpected error occurred"))

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe INTERNAL_SERVER_ERROR
            contentAsString(result) mustBe "An unexpected error occurred"
          }
        }

        "an unexpected error occurs during secondary auth call" - {
          "must return INTERNAL_SERVER_ERROR" in new Fixture {

            MockSessionDataService.getSessionData(sessionId)(sessionData)

            MockAuthConnector
              .authorise(EmptyPredicate)(Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent))))

            MockAuthConnector
              .authorise(EnrolmentHelper.agentAuthPredicate(mtditid.value))(Future.failed(InsufficientEnrolments("An error")))

            MockAuthConnector
              .authorise(EnrolmentHelper.secondaryAgentPredicate(mtditid.value))(Future.failed(new IndexOutOfBoundsException("An error")))

            mockInternalServerError(InternalServerError("An unexpected error occurred"))

            val result: Future[Result] = controller.onPageLoad()(fakeRequest)

            status(result) mustBe INTERNAL_SERVER_ERROR
            contentAsString(result) mustBe "An unexpected error occurred"
          }
        }

        "Not Authorised as a Primary Agent" - {

          "when a Secondary Agent attempts to login with VALID credentials" - {

            "must return SEE_OTHER" in new Fixture {

              MockSessionDataService.getSessionData(sessionId)(sessionData)

              val enrolments: Enrolments = Enrolments(Set(
                Enrolment(
                  key = EnrolmentKeys.SupportingAgent,
                  identifiers = Seq(EnrolmentIdentifier(EnrolmentIdentifiers.individualId, mtditid.value)),
                  state = "Activated",
                  delegatedAuthRule = Some(DelegatedAuthRules.supportingAgentDelegatedAuthRule)
                ),
                Enrolment(EnrolmentKeys.nino, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.nino, nino.value)), "Activated"),
                Enrolment(EnrolmentKeys.Agent, Seq(EnrolmentIdentifier(EnrolmentIdentifiers.agentReference, arn)), "Activated")
              ))

              MockAuthConnector
                .authorise(EmptyPredicate)(
                  Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent)))
                )

              MockAuthConnector
                .authorise(EnrolmentHelper.agentAuthPredicate(mtditid.value))(Future.failed(InsufficientEnrolments()))

              MockAuthConnector
                .authorise(EnrolmentHelper.secondaryAgentPredicate(mtditid.value))(Future.successful(enrolments))

              val result: Future[Result] = controller.onPageLoad()(fakeRequest)

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(controllers.routes.SupportingAgentAuthErrorController.show.url)
            }
          }

          "when a Secondary Agent attempts to login with INVALID credentials" - {

            "must return SEE_OTHER" in new Fixture {

              MockSessionDataService.getSessionData(sessionId)(sessionData)

              MockAuthConnector
                .authorise(EmptyPredicate)(
                  Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent)))
                )

              MockAuthConnector
                .authorise(EnrolmentHelper.agentAuthPredicate(mtditid.value))(Future.failed(InsufficientEnrolments()))

              MockAuthConnector
                .authorise(EnrolmentHelper.secondaryAgentPredicate(mtditid.value))(Future.failed(InsufficientEnrolments()))

              val result: Future[Result] = controller.onPageLoad()(fakeRequest)

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(controllers.authorisationErrors.routes.AgentAuthErrorController.onPageLoad.url)
            }
          }
        }
      }

      "when a NINO and clientID are not returned from SessionDataService" - {

        "must return SEE_OTHER (303) and redirect to Agent Error page" in new Fixture {

          MockSessionDataService.getSessionDataException(sessionId)(MissingAgentClientDetails("Missing Agent Client Details"))

          MockAuthConnector
            .authorise(EmptyPredicate)(
              Future.successful(new ~(Some("internalId"), Some(AffinityGroup.Agent)))
            )

          val result: Future[Result] = controller.onPageLoad()(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(mockAppConfig.viewAndChangeEnterUtrUrl)
        }
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in " in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(MissingBearerToken()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(mockAppConfig.signInUrl)
      }
    }

    "the user's session has expired" - {

      "must redirect the user to log in " in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(BearerTokenExpired()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must startWith(mockAppConfig.signInUrl)
      }
    }

    "the user doesn't have sufficient enrolments" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(InsufficientEnrolments()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedAuthProvider()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.UnauthorisedController.onPageLoad.url
      }
    }

    "the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedAffinityGroup()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }

    "the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in new Fixture {

        MockAuthConnector.authorise(EmptyPredicate)(Future.failed(UnsupportedCredentialRole()))

        val result: Future[Result] = controller.onPageLoad()(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad.url)
      }
    }
  }
}
