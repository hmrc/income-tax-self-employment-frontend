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
import config.FrontendAppConfig
import controllers.actions.AuthenticatedIdentifierAction.{EnrolmentIdentifiers, EnrolmentKeys, SessionValues, User}
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{allEnrolments, confidenceLevel}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent] with ActionFunction[Request, IdentifierRequest]

class AuthenticatedIdentifierAction @Inject()(
                                               override val authConnector: AuthConnector,
                                               config: FrontendAppConfig,
                                               val parser: BodyParsers.Default
                                             )
                                             (implicit val executionContext: ExecutionContext) extends IdentifierAction with AuthorisedFunctions {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    val retrievals = Retrievals.internalId and Retrievals.affinityGroup

    authorised().retrieve(retrievals) {
      case Some(internalId) ~ Some(AffinityGroup.Agent) =>
        agentAuthentication(block, internalId)(request, hc)

      case Some(internalId) ~ Some(affinityGroup) =>
        individualAuthentication(block, internalId, affinityGroup)(request, hc)

      case _ =>
        throw new UnauthorizedException("Unable to retrieve internal Id")
    } recover {
      case _: NoActiveSession =>
        Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))
      case _: AuthorisationException =>
        Redirect(routes.UnauthorisedController.onPageLoad)
    }
  }

  lazy val logger: Logger = Logger.apply(this.getClass)
  lazy val minimumConfidenceLevel: Int = ConfidenceLevel.L250.level

  private[actions] def individualAuthentication[A](block: IdentifierRequest[A] => Future[Result], internalId: String, affinityGroup: AffinityGroup)
                                                  (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {

    authorised().retrieve(allEnrolments and confidenceLevel) {
      case enrolments ~ userConfidence if userConfidence.level >= minimumConfidenceLevel =>

        val optionalMtdItId: Option[String] = enrolmentGetIdentifierValue(EnrolmentKeys.Individual, EnrolmentIdentifiers.individualId, enrolments)
        val optionalNino: Option[String] = enrolmentGetIdentifierValue(EnrolmentKeys.nino, EnrolmentIdentifiers.nino, enrolments)

        (optionalMtdItId, optionalNino) match {
          case (Some(mtdItId), Some(nino)) =>
            block(IdentifierRequest(request, internalId, User(mtdItId, arn = None, nino, affinityGroup.toString)))
          case (_, None) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no nino. Redirecting to ${config.loginUrl}")
            Future.successful(Redirect(config.loginUrl))

          case (None, _) =>
            logger.info(s"[AuthorisedAction][individualAuthentication] - User has no MTD IT enrolment. Redirecting user to sign up for MTD.")
            Future.successful(Redirect(controllers.authorisationErrors.routes.IndividualAuthErrorController.onPageLoad))
        }
      case _ =>
        logger.info("[AuthorisedAction][individualAuthentication] User has confidence level below 250.")
        Future(Redirect(config.incomeTaxSubmissionIvRedirect))
    }
  }

  private[actions] def agentAuthentication[A](block: IdentifierRequest[A] => Future[Result], internalId: String)
                                             (implicit request: Request[A], hc: HeaderCarrier): Future[Result] = {

    lazy val agentDelegatedAuthRuleKey = "mtd-it-auth"

    lazy val agentAuthPredicate: String => Enrolment = identifierId =>
      Enrolment(EnrolmentKeys.Individual)
        .withIdentifier(EnrolmentIdentifiers.individualId, identifierId)
        .withDelegatedAuthRule(agentDelegatedAuthRuleKey)

    val optionalNino = request.session.get(SessionValues.CLIENT_NINO)
    val optionalMtdItId = request.session.get(SessionValues.CLIENT_MTDITID)

    (optionalMtdItId, optionalNino) match {

      case (Some(mtdItId), Some(nino)) =>
        authorised(agentAuthPredicate(mtdItId))
          .retrieve(allEnrolments) { enrolments =>
            enrolmentGetIdentifierValue(EnrolmentKeys.Agent, EnrolmentIdentifiers.agentReference, enrolments) match {
              case Some(arn) =>
                block(IdentifierRequest(request, internalId, User(mtdItId, Some(arn), nino, AffinityGroup.Agent.toString)))
              case None =>
                logger.info("[AuthorisedAction][agentAuthentication] Agent with no HMRC-AS-AGENT enrolment. Rendering unauthorised view.")
                Future.successful(Redirect(controllers.authorisationErrors.routes.YouNeedAgentServicesController.onPageLoad))
            }
          } recover {
          case _: AuthorisationException =>
            logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have delegated authority for Client.")
            Redirect(controllers.authorisationErrors.routes.AgentAuthErrorController.onPageLoad)
        }
      case (mtditid, nino) =>
        logger.info(s"[AuthorisedAction][agentAuthentication] - Agent does not have session key values. " +
          s"Redirecting to view & change. MTDITID missing:${mtditid.isEmpty}, NINO missing:${nino.isEmpty}")
        Future.successful(Redirect(config.viewAndChangeEnterUtrUrl))
    }
  }

  private[actions] def enrolmentGetIdentifierValue(checkedKey: String,
                                                   checkedIdentifier: String,
                                                   enrolments: Enrolments): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment(`checkedKey`, enrolmentIdentifiers, _, _) => enrolmentIdentifiers.collectFirst {
      case EnrolmentIdentifier(`checkedIdentifier`, identifierValue) => identifierValue
    }
  }.flatten

}

object AuthenticatedIdentifierAction {

  case class User(mtditid: String, arn: Option[String], nino: String, affinityGroup: String) {
    def isAgent: Boolean = arn.nonEmpty
  }

  object EnrolmentKeys {
    val Individual = "HMRC-MTD-IT"
    val Agent = "HMRC-AS-AGENT"
    val nino = "HMRC-NI"
  }

  object EnrolmentIdentifiers {
    val individualId = "MTDITID"
    val agentReference = "AgentReferenceNumber"
    val nino = "NINO"
  }

  object SessionValues {
    val CLIENT_MTDITID = "ClientMTDID"
    val CLIENT_NINO = "ClientNino"

    val TAX_YEAR = "TAX_YEAR"
    val VALID_TAX_YEARS = "validTaxYears"
  }
}

