/*
 * Copyright 2024 HM Revenue & Customs
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

package controllers.journeys.adjustments.profitOrLoss

import cats.data.EitherT
import controllers.actions._
import controllers.handleApiResult
import controllers.journeys.fillForm
import forms.adjustments.profitOrLoss.WhatDoYouWantToDoWithLossFormProvider
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.errors.ServiceError
import models.journeys.adjustments.WhatDoYouWantToDoWithLoss
import models.{CheckMode, Mode}
import pages.adjustments.profitOrLoss.{CarryLossForwardPage, PreviousUnusedLossesPage, WhatDoYouWantToDoWithLossPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.adjustments.profitOrLoss.{CarryLossForwardView, WhatDoYouWantToDoWithLossView}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CurrentYearLossController @Inject() (override val messagesApi: MessagesApi,
                                           val controllerComponents: MessagesControllerComponents,
                                           service: SelfEmploymentService,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           formProvider: WhatDoYouWantToDoWithLossFormProvider,
                                           booleanFormProvider: BooleanFormProvider,
                                           whatDoYouWantToDoWithLossView: WhatDoYouWantToDoWithLossView,
                                           carryLossForwardView: CarryLossForwardView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val whatDoYouWantToDoWithLossPage = WhatDoYouWantToDoWithLossPage
  private val carryLossForwardPage          = CarryLossForwardPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      // TODO retrieve pension income in SASS-9566
      val result = service.hasOtherIncomeSources(taxYear, request.nino, request.mtditid) map {
        case true =>
          val filledForm = fillForm(whatDoYouWantToDoWithLossPage, businessId, formProvider(whatDoYouWantToDoWithLossPage, request.userType))
          Ok(whatDoYouWantToDoWithLossView(filledForm, taxYear, businessId, request.userType, mode))
        case false =>
          val filledForm = fillForm(carryLossForwardPage, businessId, booleanFormProvider(carryLossForwardPage, request.userType))
          Ok(carryLossForwardView(filledForm, taxYear, businessId, request.userType, mode))
      }
      handleApiResult(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val toCyaIfAllPagesAnswered = if (PreviousUnusedLossesPage.hasAllFurtherAnswers(businessId, request.userAnswers)) CheckMode else mode

      // TODO retrieve pension income in SASS-9566
      val result: EitherT[Future, ServiceError, Result] = service.hasOtherIncomeSources(taxYear, request.nino, request.mtditid) flatMap {
        case true =>
          def handleErrorTrue(formWithErrors: Form[_]): Result = BadRequest(
            whatDoYouWantToDoWithLossView(formWithErrors, taxYear, businessId, request.userType, mode)
          )
          def handleSuccess(answer: Set[WhatDoYouWantToDoWithLoss]): Future[Result] =
            service.persistAnswerAndRedirect(whatDoYouWantToDoWithLossPage, businessId, request, answer, taxYear, toCyaIfAllPagesAnswered)

          EitherT.right(service.handleForm(formProvider(whatDoYouWantToDoWithLossPage, request.userType), handleErrorTrue, handleSuccess))
        case false =>
          def handleErrorFalse(formWithErrors: Form[_]): Result =
            BadRequest(carryLossForwardView(formWithErrors, taxYear, businessId, request.userType, mode))

          EitherT.right(
            service.defaultHandleForm(
              booleanFormProvider(carryLossForwardPage, request.userType),
              carryLossForwardPage,
              businessId,
              taxYear,
              toCyaIfAllPagesAnswered,
              handleErrorFalse))
      }

      handleApiResult(result)

  }
}
