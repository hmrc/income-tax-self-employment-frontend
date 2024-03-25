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
import forms.capitalallowances.specialTaxSites.SpecialTaxSiteLocationFormProvider
import forms.capitalallowances.specialTaxSites.SpecialTaxSiteLocationFormProvider.filterErrors
import models.Mode
import models.common.{BusinessId, TaxYear}
import pages.capitalallowances.specialTaxSites.SpecialTaxSiteLocationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocationView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SpecialTaxSiteLocationController @Inject() (override val messagesApi: MessagesApi,
                                                  val controllerComponents: MessagesControllerComponents,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction,
                                                  requireData: DataRequiredAction,
                                                  service: SpecialTaxSitesService,
                                                  formProvider: SpecialTaxSiteLocationFormProvider,
                                                  view: SpecialTaxSiteLocationView)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = SpecialTaxSiteLocationPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val filledForm = page.fillFormWithIndex(formProvider(request.userType), page, request, businessId, index)
      Ok(view(filledForm, mode, request.userType, taxYear, businessId, index))
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Int, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) async { implicit request =>
      formProvider(request.userType)
        .bindFromRequest()
        .fold(
          formErrors =>
            Future.successful(BadRequest(view(filterErrors(formErrors, request.userType), mode, request.userType, taxYear, businessId, index))),
          answer => service.updateAndRedirectWithIndex(request.userAnswers, answer, businessId, taxYear, index, page)
        )
    }

}
