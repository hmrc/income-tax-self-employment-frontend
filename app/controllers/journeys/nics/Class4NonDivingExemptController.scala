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
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.BusinessId.nationalInsuranceContributions
import models.common.{BusinessId, TaxYear}
import pages.nics.Class4NonDivingExemptPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.nics.Class4NonDivingExemptView

import javax.inject.{Inject, Singleton}

@Singleton
class Class4NonDivingExemptController @Inject() (override val messagesApi: MessagesApi,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 requireData: DataRequiredAction,
                                                 formProvider: BooleanFormProvider,
                                                 service: SelfEmploymentService,
                                                 view: Class4NonDivingExemptView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = Class4NonDivingExemptPage

  def onPageLoad(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    val form = formProvider(page, request.userType)
    Ok(view(form, taxYear, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    service.persistAnswerAndRedirect(
      page,
      nationalInsuranceContributions,
      request,
      List(BusinessId("business1")),
      taxYear,
      mode
    ) // TODO: Proper impl will be in next story
  }

}
