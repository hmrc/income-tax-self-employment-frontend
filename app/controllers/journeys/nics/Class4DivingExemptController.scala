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

package controllers.journeys.nics

import controllers.actions._
import forms.nics.Class4ExemptBusinessesFormProvider
import models.Mode
import models.common.BusinessId.{classFourOtherExemption, emptyBusinessId, nationalInsuranceContributions}
import models.common.TaxYear
import models.requests.DataRequest
import pages.nics.Class4DivingExemptPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.nics.Class4DivingExemptView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class Class4DivingExemptController @Inject() (override val messagesApi: MessagesApi,
                                              val controllerComponents: MessagesControllerComponents,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              requireData: DataRequiredAction,
                                              formProvider: Class4ExemptBusinessesFormProvider,
                                              service: SelfEmploymentService,
                                              view: Class4DivingExemptView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = Class4DivingExemptPage

  private def businesses(implicit request: DataRequest[AnyContent]) = request.userAnswers.getBusinesses

  def onPageLoad(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form = formProvider(page, request.userType)
    Ok(view(form, taxYear, request.userType, mode, businesses))
  }

  def onSubmit(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    formProvider(page, request.userType)
      .bindFromRequest()
      .fold(
        formErrors => Future.successful(BadRequest(view(formErrors, taxYear, request.userType, mode, businesses))),
        answer => {
          val filteredAnswer = if (answer.contains(emptyBusinessId)) List(classFourOtherExemption) else answer
          service.submitGatewayQuestionAndRedirect(page, nationalInsuranceContributions, request.userAnswers, filteredAnswer, taxYear, mode)
        }
      )
  }

}
