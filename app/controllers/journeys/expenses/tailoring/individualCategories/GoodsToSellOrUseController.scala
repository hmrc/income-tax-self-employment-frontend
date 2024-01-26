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

package controllers.journeys.expenses.tailoring.individualCategories

import cats.data.EitherT
import controllers.actions._
import controllers.{handleApiResult, handleResultT}
import forms.expenses.tailoring.individualCategories.GoodsToSellOrUseFormProvider
import models.Mode
import models.common.{AccountingType, BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import models.errors.ServiceError
import models.journeys.expenses.individualCategories.{GoodsToSellOrUse, TaxiMinicabOrRoadHaulage}
import navigation.ExpensesTailoringNavigator
import pages.expenses.tailoring.individualCategories.{GoodsToSellOrUsePage, TaxiMinicabOrRoadHaulagePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.tailoring.individualCategories.GoodsToSellOrUseView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GoodsToSellOrUseController @Inject() (override val messagesApi: MessagesApi,
                                            selfEmploymentService: SelfEmploymentService,
                                            navigator: ExpensesTailoringNavigator,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            formProvider: GoodsToSellOrUseFormProvider,
                                            val controllerComponents: MessagesControllerComponents,
                                            view: GoodsToSellOrUseView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val isTaxiDriver = request.userAnswers.get(TaxiMinicabOrRoadHaulagePage, Some(businessId)).contains(TaxiMinicabOrRoadHaulage.Yes)
      for {
        accountingType <- handleApiResult(selfEmploymentService.getAccountingType(request.nino, businessId, request.mtditid))
        userType       = request.userType
        existingAnswer = request.userAnswers.get(GoodsToSellOrUsePage, Some(businessId))
        form           = formProvider(userType)
        preparedForm   = existingAnswer.fold(form)(form.fill)
      } yield Ok(view(preparedForm, mode, request.userType, taxYear, businessId, accountingType, isTaxiDriver))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(userAnswers: UserAnswers, value: GoodsToSellOrUse): Future[Result] =
        selfEmploymentService
          .persistAnswer(businessId, userAnswers, value, GoodsToSellOrUsePage)
          .map(updated => Redirect(navigator.nextPage(GoodsToSellOrUsePage, mode, updated, taxYear, businessId)))

      def handleForm(form: Form[GoodsToSellOrUse],
                     userType: UserType,
                     accountingType: AccountingType,
                     isTaxiDriver: Boolean,
                     userAnswers: UserAnswers): Either[Future[Result], Future[Result]] =
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Left(Future.successful(BadRequest(view(formWithErrors, mode, userType, taxYear, businessId, accountingType, isTaxiDriver)))),
            value => Right(handleSuccess(userAnswers, value))
          )

      val isTaxiDriver = request.userAnswers.get(TaxiMinicabOrRoadHaulagePage, Some(businessId)).contains(TaxiMinicabOrRoadHaulage.Yes)
      for {
        accountingType <- handleApiResult(selfEmploymentService.getAccountingType(request.nino, businessId, request.mtditid))
        userType    = request.userType
        userAnswers = request.userAnswers
        form        = formProvider(userType)
        finalResult <- handleResultT(EitherT.right[ServiceError](handleForm(form, userType, accountingType, isTaxiDriver, userAnswers).merge))
      } yield finalResult
  }

}
