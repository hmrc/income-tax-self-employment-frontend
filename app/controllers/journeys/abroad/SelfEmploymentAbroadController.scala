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

package controllers.journeys.abroad

import controllers.actions._
import controllers.journeys.fillForm
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import navigation.AbroadNavigator
import pages.abroad.SelfEmploymentAbroadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.abroad.SelfEmploymentAbroadView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfEmploymentAbroadController @Inject() (override val messagesApi: MessagesApi,
                                                val controllerComponents: MessagesControllerComponents,
                                                identify: IdentifierAction,
                                                navigator: AbroadNavigator,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: BooleanFormProvider,
                                                service: SelfEmploymentService,
                                                view: SelfEmploymentAbroadView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = SelfEmploymentAbroadPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form = fillForm(page, businessId, formProvider(page, request.userType))
      Ok(view(form, taxYear, businessId, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode))),
          value =>
            service
              .persistAnswer(businessId, request.userAnswers, value, SelfEmploymentAbroadPage)
              .map(updated => Redirect(navigator.nextPage(SelfEmploymentAbroadPage, mode, updated, taxYear, businessId)))
        )
  }

}
