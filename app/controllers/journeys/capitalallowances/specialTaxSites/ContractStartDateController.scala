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

package controllers.journeys.capitalallowances.specialTaxSites

import cats.implicits.catsSyntaxOptionId
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.DateFormModel
import forms.capitalallowances.specialTaxSites.ContractStartDateFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.specialTaxSites.ContractStartDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.specialTaxSites.ContractStartDateView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ContractStartDateController @Inject() (override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             getData: DataRetrievalAction,
                                             requireData: DataRequiredAction,
                                             service: SelfEmploymentService,
                                             val controllerComponents: MessagesControllerComponents,
                                             view: ContractStartDateView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val formProvider = ContractStartDateFormProvider.formProvider
      val form = request.userAnswers
        .get(ContractStartDatePage, businessId.some)
        .fold(formProvider) { localDate: LocalDate => formProvider.fill(DateFormModel(localDate)) }

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      ContractStartDateFormProvider.formProvider
        .bindFromRequest()
        .fold(
          formErrors => {
            val (filteredFormErrors, hasWholeFormError) = ContractStartDateFormProvider.checkForWholeFormErrors(formErrors, request.userType)
            Future.successful(BadRequest(view(filteredFormErrors, mode, request.userType, taxYear, businessId, hasWholeFormError)))
          },
          answer =>
            service.persistAnswer(businessId, request.userAnswers, answer.toLocalDate, ContractStartDatePage).map {
              ContractStartDatePage.redirectNext(mode, _, businessId, taxYear)
            }
        )
  }

}
