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
import models.common.{BusinessId, JourneyContext, Mtditid}
import models.database.UserAnswers
import models.errors.HttpError
import models.journeys.Journey
import models.requests.OptionalDataRequest
import play.api.libs.json.{Format, JsObject, Json}
import play.api.mvc.ActionTransformer
import repositories.SessionRepositoryBase
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendHeaderCarrierProvider
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

class SubmittedDataRetrievalActionImpl[SubsetOfAnswers: Format](journeyContext: Mtditid => JourneyContext,
                                                                selfEmploymentService: SelfEmploymentServiceBase,
                                                                sessionRepository: SessionRepositoryBase)(implicit ec: ExecutionContext)
    extends ActionTransformer[OptionalDataRequest, OptionalDataRequest]
    with FrontendHeaderCarrierProvider
    with Logging {

  protected def executionContext: ExecutionContext = ec

  protected[actions] def transform[A](request: OptionalDataRequest[A]): Future[OptionalDataRequest[A]] = {
    val ctx                      = journeyContext(request.mtditid)
    val containsShortTermAnswers = containsAtLeastOneJourneyShortTermAnswer(ctx.businessId, ctx.journey, request.userAnswers)

    if (containsShortTermAnswers) {
      Future.successful(request)
    } else {
      val result: EitherT[Future, HttpError, OptionalDataRequest[A]] = for {
        submittedAnswers <- selfEmploymentService.getSubmittedAnswers[SubsetOfAnswers](ctx)(implicitly[Format[SubsetOfAnswers]], hc(request.request))
        maybeRequestWithAnswers <- EitherT.right(submittedAnswers.traverse { answers =>
          insertSubmittedAnswersToSession(request, ctx.businessId, answers)
        })
      } yield maybeRequestWithAnswers.getOrElse(request)

      result.fold(
        err => {
          logger.error(s"Error while getting submitted data: ${err.body.toString}, status=${err.status}")
          request
        },
        x => x)
    }
  }

  private def insertSubmittedAnswersToSession[A](request: OptionalDataRequest[A],
                                                 businessId: BusinessId,
                                                 answers: SubsetOfAnswers): Future[OptionalDataRequest[A]] = {
    val data                 = Json.toJson(answers).as[JsObject]
    val userShortTermAnswers = UserAnswers(request.userId, Json.obj(businessId.value -> data))

    sessionRepository
      .set(userShortTermAnswers)
      .map(_ =>
        OptionalDataRequest(
          request.request,
          request.userId,
          request.user,
          Some(userShortTermAnswers)
        ))
  }

  private def containsAtLeastOneJourneyShortTermAnswer(businessId: BusinessId, journey: Journey, maybeUserAnswers: Option[UserAnswers]) =
    maybeUserAnswers.exists { userAnswers =>
      val journeyAnswers = (userAnswers.data \ businessId.value).asOpt[JsObject].getOrElse(Json.obj())
      journey.pageKeys.exists(page => journeyAnswers.keys.contains(page.value))
    }

}
