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
import models.common.BusinessId.{classFourNoneExempt, nationalInsuranceContributions}
import models.common.{Business, TaxYear}
import pages.nics.Class4NonDivingExemptSingleBusinessPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.nics.Class4NonDivingExemptYesNoView

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class Class4NonDivingExemptSingleBusinessController @Inject() (override val messagesApi: MessagesApi,
                                                               val controllerComponents: MessagesControllerComponents,
                                                               identify: IdentifierAction,
                                                               getData: DataRetrievalAction,
                                                               requireData: DataRequiredAction,
                                                               formProvider: BooleanFormProvider,
                                                               service: SelfEmploymentService,
                                                               view: Class4NonDivingExemptYesNoView)
    extends FrontendBaseController
    with I18nSupport {

  private val page = Class4NonDivingExemptSingleBusinessPage

  def onPageLoad(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) { implicit request =>
    page.remainingBusinesses(request.userAnswers) match {
      case List(Business(_, tradingName)) =>
        Ok(
          view(
            formProvider(page, request.userType, parameters = List(s"${tradingName.value}.")),
            taxYear,
            request.userType,
            mode,
            tradingName.value
          ))
      case invalidList => InternalServerError(s"Controller called with invalid remainingBusinesses list ${invalidList.mkString("[", ",", "]")}")
    }
  }

  def onSubmit(taxYear: TaxYear, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    def handleError(formWithErrors: Form[_]): Result =
      BadRequest(view(formWithErrors, taxYear, request.userType, mode, page.remainingBusinesses(request.userAnswers).head.tradingName.value))

    def handleSuccess(answer: Boolean): Future[Result] =
      if (answer) {
        service.persistAnswerAndRedirect(
          page,
          nationalInsuranceContributions,
          request,
          page.remainingBusinesses(request.userAnswers).map(_.businessId),
          taxYear,
          mode)
      } else {
        service.persistAnswerAndRedirect(page, nationalInsuranceContributions, request, List(classFourNoneExempt), taxYear, mode)
      }

    val tradingName = page.remainingBusinesses(request.userAnswers).headOption.map(_.tradingName.value).getOrElse("")

    service.handleForm(formProvider(page, request.userType, parameters = List(s"$tradingName.")), handleError, handleSuccess)
  }
}
