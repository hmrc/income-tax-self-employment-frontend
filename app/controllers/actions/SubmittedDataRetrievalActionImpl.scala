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
import controllers.handleApiResult
import models.common.{BusinessId, JourneyContext, Mtditid, UserId}
import models.database.UserAnswers
import models.domain.ApiResultT
import models.errors.HttpError
import models.journeys.Journey
import models.requests.OptionalDataRequest
import play.api.libs.json.{Format, JsObject, Json}
import play.api.mvc.{ActionTransformer, Request}
import repositories.SessionRepositoryBase
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SubmittedDataRetrievalAction extends ActionTransformer[OptionalDataRequest, OptionalDataRequest]

class SubmittedDataRetrievalActionImpl[SubsetOfAnswers: Format](journeyContext: Mtditid => JourneyContext,
                                                                selfEmploymentService: SelfEmploymentServiceBase,
                                                                sessionRepository: SessionRepositoryBase)(implicit ec: ExecutionContext)
    extends SubmittedDataRetrievalAction
    with FrontendHeaderCarrierProvider
    with Logging {

  protected def executionContext: ExecutionContext = ec

  protected[actions] def transform[A](request: OptionalDataRequest[A]): Future[OptionalDataRequest[A]] = {
    val ctx: JourneyContext     = journeyContext(request.mtditid)
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
      maybeJourneyAnswers <- selfEmploymentService.getSubmittedAnswers[SubsetOfAnswers](ctx)(implicitly[Format[SubsetOfAnswers]], hc(request))
      updatedAnswers <- maybeJourneyAnswers.fold {
        EitherT.pure[Future, HttpError](existingAnswers)
      } { answers =>
        val jsonAnswers = Json.toJson(answers).as[JsObject]
        val updatedAnswers = existingAnswers.upsertFragment(ctx.businessId, jsonAnswers) match {
          case Success(updated) => updated.asRight
          case Failure(err)     => HttpError.internalError(err).asLeft
        }
        updateSessionRepository(updatedAnswers)
      }
    } yield updatedAnswers

    handleApiResult(result)
  }

  private def updateSessionRepository(maybeUpdatedAnswers: Either[HttpError, UserAnswers]) =
    for {
      updated <- EitherT.fromEither[Future](maybeUpdatedAnswers)
      _       <- EitherT.right[HttpError](sessionRepository.set(updated))
    } yield updated

  private def hasAtLeastOneUserAnswerForJourney(businessId: BusinessId, journey: Journey, maybeUserAnswers: Option[UserAnswers]): Boolean =
    maybeUserAnswers.exists { userAnswers =>
      val journeyAnswers = (userAnswers.data \ businessId.value).asOpt[JsObject].getOrElse(Json.obj())
      journey.pageKeys.exists(page => journeyAnswers.keys.contains(page.value))
    }

}
