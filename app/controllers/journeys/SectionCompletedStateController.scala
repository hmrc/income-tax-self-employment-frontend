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

package controllers.journeys

import controllers._
import controllers.actions._
import forms.SectionCompletedStateFormProvider
import models.Mode
import models.common.JourneyStatus._
import models.common.{BusinessId, JourneyAnswersContext, JourneyStatus, TaxYear}
import models.journeys.CompletedSectionState.{No, Yes}
import models.journeys.{CompletedSectionState, Journey}
import navigation.GeneralNavigator
import pages.SectionCompletedStatePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SelfEmploymentServiceBase
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.SectionCompletedStateView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SectionCompletedStateController @Inject() (override val messagesApi: MessagesApi,
                                                 service: SelfEmploymentServiceBase,
                                                 navigator: GeneralNavigator,
                                                 identify: IdentifierAction,
                                                 getData: DataRetrievalAction,
                                                 formProvider: SectionCompletedStateFormProvider,
                                                 val controllerComponents: MessagesControllerComponents,
                                                 view: SectionCompletedStateView)(implicit val ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[CompletedSectionState] = formProvider()

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async {
    implicit request =>
      val preparedForm = service
        .getJourneyStatus(JourneyAnswersContext(taxYear, businessId, request.mtditid, Journey.withName(journey)))
        .value
        .map(_.fold(_ => form, fill(form, _)))

      preparedForm map { form =>
        Ok(view(form, taxYear, businessId, Journey.withName(journey), mode))
      }
  }

  private def fill(form: Form[CompletedSectionState], status: JourneyStatus) =
    status match {
      case Completed                                     => form.fill(Yes)
      case InProgress                                    => form.fill(No)
      case NotStarted | CheckOurRecords | CannotStartYet => form
    }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, journey: String, mode: Mode): Action[AnyContent] = (identify andThen getData) async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, Journey.withName(journey), mode))),
          state =>
            handleResultT(
              saveAndRedirect(JourneyAnswersContext(taxYear, businessId, request.mtditid, Journey.withName(journey)), state)
            )
        )
  }

  private def saveAndRedirect(ctx: JourneyAnswersContext, state: CompletedSectionState)(implicit hc: HeaderCarrier) =
    service.setJourneyStatus(ctx, state.toStatus).map { _ =>
      Redirect(navigator.nextPage(SectionCompletedStatePage, ctx.taxYear))
    }

}
