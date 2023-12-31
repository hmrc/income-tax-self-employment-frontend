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
import forms.abroad.SelfEmploymentAbroadFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.database.UserAnswers
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
                                                selfEmploymentService: SelfEmploymentService,
                                                navigator: AbroadNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                formProvider: SelfEmploymentAbroadFormProvider,
                                                val controllerComponents: MessagesControllerComponents,
                                                view: SelfEmploymentAbroadView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.userId)).get(SelfEmploymentAbroadPage, Some(businessId)) match {
      case None        => formProvider(request.userType)
      case Some(value) => formProvider(request.userType).fill(value)
    }

    Ok(view(preparedForm, taxYear, businessId, request.userType, mode))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    formProvider(request.userType)
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode))),
        value =>
          selfEmploymentService
            .persistAnswer(businessId, request.userAnswers.getOrElse(UserAnswers(request.userId)), value, SelfEmploymentAbroadPage)
            .map(updated => Redirect(navigator.nextPage(SelfEmploymentAbroadPage, mode, updated, taxYear, businessId)))
      )
  }

}
