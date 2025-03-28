/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers.journeys.expenses.travelAndAccommodation

import controllers.actions._
import forms.standard.CurrencyFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear, UserType}
import navigation.TravelAndAccommodationNavigator
import pages.CostsNotCoveredPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.expenses.travelAndAccommodation.CostsNotCoveredView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CostsNotCoveredController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    navigator: TravelAndAccommodationNavigator,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: CurrencyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: CostsNotCoveredView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = CostsNotCoveredPage
  private val form = (userType: UserType) =>
    formProvider(
      page = page,
      userType = userType,
      minValueError = s"costsNotCovered.error.lessThanZero.$userType",
      maxValueError = s"costsNotCovered.error.overMax.$userType",
      nonNumericError = s"costsNotCovered.error.nonNumeric.$userType"
    )

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(CostsNotCoveredPage, businessId) match {
        case None        => form(request.userType)
        case Some(value) => form(request.userType).fill(value)
      }

      Ok(view(preparedForm, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form(request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(CostsNotCoveredPage, value, Some(businessId)))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(CostsNotCoveredPage, mode, updatedAnswers, taxYear, businessId))
        )
  }
}
