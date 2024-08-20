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

import cats.data.EitherT
import cats.implicits._
import connectors.ContentHttpReads
import connectors.SelfEmploymentConnector
import controllers.handleApiResult
import models.common.BusinessId
import models.common.JourneyContext
import models.common.UserId
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.ServiceError
import models.journeys.Journey
import models.journeys.Journey.NationalInsuranceContributions
import models.requests.OptionalDataRequest
import play.api.libs.json.Format
import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.mvc.ActionTransformer
import play.api.mvc.Request
import repositories.SessionRepositoryBase
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import utils.Logging

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait SubmittedDataRetrievalAction extends ActionTransformer[OptionalDataRequest, OptionalDataRequest] {
  def execute[A](request: OptionalDataRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
}

class SubmittedDataRetrievalActionImpl[SubsetOfAnswers: Format](journeyContext: OptionalDataRequest[_] => JourneyContext,
                                                                connector: SelfEmploymentConnector,
                                                                sessionRepository: SessionRepositoryBase)(implicit ec: ExecutionContext)
    extends SubmittedDataRetrievalAction
    with FrontendHeaderCarrierProvider
    with Logging {

  protected def executionContext: ExecutionContext = ec

  protected[actions] def transform[A](request: OptionalDataRequest[A]): Future[OptionalDataRequest[A]] = {
    val ctx: JourneyContext     = journeyContext(request)
    val hasAtLeastOneUserAnswer = hasAtLeastOneUserAnswerForJourney(ctx.businessId, ctx.journey, request.userAnswers)

    if (hasAtLeastOneUserAnswer) {
      Future.successful(request)
    } else {
      val existingUserAnswers = request.userAnswers.getOrElse(UserAnswers.empty(UserId(request.userId)))
      upsertJourneyAnswers(ctx, request.request, existingUserAnswers).map { updated =>
        request.copy(userAnswers = if (updated.isEmpty) None else Some(updated))
      }
    }
  }

  private def upsertJourneyAnswers[A](ctx: JourneyContext, request: Request[A], existingAnswers: UserAnswers): Future[UserAnswers] = {
    val result: ApiResultT[UserAnswers] = for {
      answers <- getSubmittedAnswers(ctx)(hc(request))
      updatedAnswers = answers.map { a =>
        if (ctx.journey == NationalInsuranceContributions) existingAnswers.upsertFragmentNICs(ctx.businessId, ContentHttpReads.asJsonUnsafe(a))
        else existingAnswers.upsertFragment(ctx.businessId, ContentHttpReads.asJsonUnsafe(a))
      }
      _ <- EitherT.right[ServiceError](updatedAnswers.fold(Future.successful(()))(sessionRepository.set(_).void))
    } yield updatedAnswers.getOrElse(existingAnswers)

    handleApiResult(result)
  }

  private def getSubmittedAnswers(context: JourneyContext)(implicit hc: HeaderCarrier): ApiResultT[Option[SubsetOfAnswers]] =
    connector.getSubmittedAnswers[SubsetOfAnswers](context)

  private def hasAtLeastOneUserAnswerForJourney(businessId: BusinessId, journey: Journey, maybeUserAnswers: Option[UserAnswers]): Boolean =
    maybeUserAnswers.exists { userAnswers =>
      val journeyAnswers = (userAnswers.data \ businessId.value).asOpt[JsObject].getOrElse(Json.obj())
      journey.pageKeys.exists(page => journeyAnswers.keys.contains(page.value))
    }

}
