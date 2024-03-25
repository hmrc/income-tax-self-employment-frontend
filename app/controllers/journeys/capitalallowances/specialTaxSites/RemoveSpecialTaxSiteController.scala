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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.specialTaxSites._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.specialTaxSites.RemoveSpecialTaxSiteView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveSpecialTaxSiteController @Inject() (override val messagesApi: MessagesApi,
                                                val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                service: SelfEmploymentService,
                                                formProvider: BooleanFormProvider,
                                                view: RemoveSpecialTaxSiteView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = RemoveSpecialTaxSitePage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(formProvider(page, request.userType), request.userType, taxYear, businessId, index))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleSuccess(answer: Boolean): Future[Result] = {
        val sitesList         = request.getValue(NewSpecialTaxSitesList, businessId)
        val indexIsValid      = sitesList.exists(index >= 0 && index < _.length)
        val continueToSummary = Redirect(routes.NewTaxSitesController.onPageLoad(taxYear, businessId))
        (answer, indexIsValid, sitesList) match {
          case (true, true, Some(list)) =>
            val updatedList = list.patch(index, Nil, 1)
            service.persistAnswer(businessId, request.userAnswers, updatedList, NewSpecialTaxSitesList).map { _ =>
              continueToSummary
            }
          case _ => Future.successful(continueToSummary)
        }
      }

      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, request.userType, taxYear, businessId, index))),
          answer => handleSuccess(answer)
        )
  }

}
