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

import com.google.inject.Inject
import common._
import config.FrontendAppConfig
import handlers.ErrorHandler
import models.errors.ServiceError.MissingAgentClientDetails
import models.requests.{IdentifierRequest, User}
import play.api.mvc.Results._
import play.api.mvc._
import services.SessionDataService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{EnrolmentHelper, SessionHelper}

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject() (
    override val authConnector: AuthConnector,
    errorHandler: ErrorHandler,
    override val config: FrontendAppConfig,
    sessionDataService: SessionDataService,
    val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with SessionHelper {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    implicit val req: Request[A]   = request

    withSessionId { sessionId =>
      authorised().retrieve(Retrievals.internalId and Retrievals.affinityGroup) {
        case Some(internalId) ~ Some(AffinityGroup.Agent) =>
          agentAuthentication(block, internalId, sessionId)(request, hc)
        case Some(internalId) ~ Some(affinityGroup) =>
          individualAuthentication(block, internalId, sessionId, affinityGroup)(request, hc)
        case _ =>
          throw new UnauthorizedException("Unable to retrieve internalId or affinityGroup from Auth")
      } recover {
        case _: NoActiveSession =>
          Redirect(config.signInUrl)
        case _: AuthorisationException =>
          Redirect(controllers.standard.routes.UnauthorisedController.onPageLoad)
        case e =>
          logger.error(s"[AuthorisedAction][invokeBlock] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught")
          errorHandler.internalServerError()(request)
      }
    }
  }

  private[actions] def individualAuthentication[A](block: IdentifierRequest[A] => Future[Result],
                                                   internalId: String,
                                                   sessionId: String,
                                                   affinityGroup: AffinityGroup)(implicit request: Request[A], hc: HeaderCarrier): Future[Result] =
    authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= ConfidenceLevel.L250.level =>
        (
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments),
          EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.nino, EnrolmentIdentifiers.nino, enrolments)
        ) match {
          case (Some(mtdItId), Some(nino)) =>
            block(IdentifierRequest(request, internalId, User(mtdItId, arn = None, nino, sessionId, affinityGroup.toString)))
          case (_, None) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no nino. Redirecting to sign in")
            Future.successful(Redirect(config.signInUrl))
          case (None, _) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD.")
            Future.successful(Redirect(controllers.authorisationErrors.routes.IndividualAuthErrorController.onPageLoad))
        }
      case _ =>
        logger.info("[AuthorisedAction][individualAuthentication] User has confidence level below 250.")
        Future(Redirect(config.incomeTaxSubmissionIvRedirect))
    }

  private[actions] def agentAuthentication[A](block: IdentifierRequest[A] => Future[Result], internalId: String, sessionId: String)(implicit
      request: Request[A],
      hc: HeaderCarrier): Future[Result] =
    sessionDataService
      .getSessionData(sessionId)
      .flatMap { sessionData =>
        authorised(EnrolmentHelper.agentAuthPredicate(sessionData.mtditid))
          .retrieve(allEnrolments)(populateAgent(block, internalId, sessionData.mtditid, sessionData.nino, sessionId, _, isSupportingAgent = false))
          .recoverWith(agentRecovery(block, internalId, sessionData.mtditid, sessionData.nino, sessionId))
      }
      .recover { case _: MissingAgentClientDetails =>
        Redirect(config.viewAndChangeEnterUtrUrl)
      }

  private def agentRecovery[A](block: IdentifierRequest[A] => Future[Result], internalId: String, mtdItId: String, nino: String, sessionId: String)(
      implicit
      request: Request[A],
      hc: HeaderCarrier): PartialFunction[Throwable, Future[Result]] = {
    case _: AuthorisationException =>
      authorised(EnrolmentHelper.secondaryAgentPredicate(mtdItId))
        .retrieve(allEnrolments) {
          populateAgent(block, internalId, mtdItId, nino, sessionId, _, isSupportingAgent = true)
        }
        .recover {
          case _: AuthorisationException =>
            logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client.")
            Redirect(controllers.authorisationErrors.routes.AgentAuthErrorController.onPageLoad)
          case e =>
            logger.error(s"[AuthorisedAction][agentAuthentication] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught")
            errorHandler.internalServerError()
        }
    case e =>
      logger.error(s"[AuthorisedAction][agentAuthentication] - Unexpected exception of type '${e.getClass.getSimpleName}' was caught")
      Future.successful(errorHandler.internalServerError())
  }

  private def populateAgent[A](block: IdentifierRequest[A] => Future[Result],
                               internalId: String,
                               mtdItId: String,
                               nino: String,
                               sessionId: String,
                               enrolments: Enrolments,
                               isSupportingAgent: Boolean)(implicit request: Request[A]) =
    if (isSupportingAgent) {
      logger.info(s"[AuthorisedAction][agentAuthentication] - Secondary agent unauthorised")
      Future.successful(Redirect(controllers.routes.SupportingAgentAuthErrorController.show))
    } else {
      EnrolmentHelper.getEnrolmentValueOpt(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
        case Some(arn) =>
          block(IdentifierRequest(request, internalId, User(mtdItId, Some(arn), nino, sessionId, AffinityGroup.Agent.toString, isSupportingAgent)))
        case None =>
          logger.info(s"[AuthorisedAction][agentAuthentication] - Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
          Future.successful(Redirect(controllers.authorisationErrors.routes.AgentAuthErrorController.onPageLoad))
      }
    }
}
