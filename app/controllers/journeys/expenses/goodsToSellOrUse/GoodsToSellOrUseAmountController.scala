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

package controllers.journeys.expenses.goodsToSellOrUse

import controllers.actions._
import controllers.standard.routes.JourneyRecoveryController
import forms.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountFormProvider
import models.Mode
import models.common.ModelUtils.userType
import models.database.UserAnswers
import models.journeys.expenses.TaxiMinicabOrRoadHaulage
import navigation.ExpensesNavigator
import pages.expenses.TaxiMinicabOrRoadHaulagePage
import pages.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountPage
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.ContentStringViewModel.buildLabelHeadingWithContentString
import views.html.journeys.expenses.goodsToSellOrUse.GoodsToSellOrUseAmountView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GoodsToSellOrUseAmountController @Inject() (override val messagesApi: MessagesApi,
                                                  selfEmploymentService: SelfEmploymentService,
                                                  sessionRepository: SessionRepository,
                                                  navigator: ExpensesNavigator,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  formProvider: GoodsToSellOrUseAmountFormProvider,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  view: GoodsToSellOrUseAmountView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val businessId   = "SJPR05893938418"
  val taxYear: Int = LocalDate.now.getYear
  val isAccrual    = true
  val isTaxiDriver = true

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) map {
      case Left(_) => Redirect(JourneyRecoveryController.onPageLoad())
      case Right(accountingType) =>
        val user = userType(request.user.isAgent)
        val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(GoodsToSellOrUseAmountPage, Some(businessId)) match {
          case None        => formProvider(user)
          case Some(value) => formProvider(user).fill(value)
        }
        val taxiDriver = request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(TaxiMinicabOrRoadHaulagePage, Some(businessId))
          .contains(TaxiMinicabOrRoadHaulage.Yes)
        Ok(view(preparedForm, mode, user, taxYear, businessId, accountingType, taxiDriver, labelContent(user, isAccrual, isTaxiDriver)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData) async { implicit request =>
    selfEmploymentService.getAccountingType(request.user.nino, businessId, request.user.mtditid) flatMap {
      case Left(_) => Future.successful(Redirect(JourneyRecoveryController.onPageLoad()))
      case Right(accountingType) =>
        val user = userType(request.user.isAgent)
        val taxiDriver = request.userAnswers
          .getOrElse(UserAnswers(request.userId))
          .get(TaxiMinicabOrRoadHaulagePage, Some(businessId))
          .contains(TaxiMinicabOrRoadHaulage.Yes)
        val form = formProvider(user)
        form
          .bindFromRequest()
          .fold(
            formWithErrors =>
              Future.successful(BadRequest(
                view(formWithErrors, mode, user, taxYear, businessId, accountingType, taxiDriver, labelContent(user, isAccrual, isTaxiDriver)))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(
                  request.userAnswers.getOrElse(UserAnswers(request.userId)).set(GoodsToSellOrUseAmountPage, value, Some(businessId)))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(GoodsToSellOrUseAmountPage, mode, updatedAnswers))
          )
    }
  }

  private def labelContent(userType: String, isAccrual: Boolean, isTaxiDriver: Boolean)(implicit messages: Messages): String = {

    val detailsContent =
      s"""
         | <details class="govuk-details govuk-!-margin-bottom-3" data-module="govuk-details">
         |   <summary class="govuk-details__summary">
         |     <span class="govuk-details__summary-text">
         |       ${messages("goodsToSellOrUseAmount.d1.heading")}
         |      </span>
         |   </summary>
         |   <div class="govuk-details__text">
         |      <p>${messages(s"site.canInclude.$userType")}</p>
         |      <ul class="govuk-body govuk-list--bullet">
         |        ${if (isTaxiDriver) s"""<li>${messages("expenses.fuelCosts")}</li>"""}
         |        <li>${messages("expenses.costOfRawMaterials")}</li>
         |        <li>${messages("expenses.stockBought")}</li>
         |        <li>${messages("expenses.directCostsOfProducing")}</li>
         |        ${if (!isAccrual) s"""<li>${messages("expenses.adjustments")}</li>"""}
         |        <li>${messages("expenses.commissions")}</li>
         |        <li>${messages("expenses.discounts")}</li>
         |      </ul>
         |      <p>${messages(s"site.cannotInclude.$userType")}</p>
         |      <ul class="govuk-body govuk-list--bullet">
         |        ${if (!isAccrual) s"""<li>${messages("expenses.costsForPrivateUse")}</li>"""}
         |        <li>${messages("expenses.depreciationOfEquipment")}</li>
         |      </ul>
         |    </div>
         | </details>
         |""".stripMargin

    buildLabelHeadingWithContentString(
      s"goodsToSellOrUseAmount.title.$userType",
      detailsContent,
      headingClasses = "govuk-label govuk-label--l"
    )
  }

}
