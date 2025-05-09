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

package controllers.journeys.industrysectors

import controllers.actions._
import forms.standard.BooleanFormProvider
import models.Mode
import models.common.Journey.IndustrySectors
import models.common.{BusinessId, TaxYear}
import models.journeys.industrySectors.IndustrySectorsDb
import navigation.IndustrySectorsNavigator
import pages.industrysectors.SelfEmploymentAbroadPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.answers.AnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.journeys.industrysectors.SelfEmploymentAbroadView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SelfEmploymentAbroadController @Inject() (override val messagesApi: MessagesApi,
                                                val controllerComponents: MessagesControllerComponents,
                                                navigator: IndustrySectorsNavigator,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                requireData: DataRequiredAction,
                                                formProvider: BooleanFormProvider,
                                                answersService: AnswersService,
                                                view: SelfEmploymentAbroadView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val page = SelfEmploymentAbroadPage

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, IndustrySectors)
      answersService.getAnswers[IndustrySectorsDb](ctx).map { optIndustrySectorDetails =>
        val form: Form[Boolean] = formProvider(page, request.userType)
        val preparedForm        = optIndustrySectorDetails.flatMap(_.isAllSelfEmploymentAbroad).fold(form)(form.fill)
        Ok(view(preparedForm, taxYear, businessId, request.userType, mode))
      }
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val ctx = request.mkJourneyNinoContext(taxYear, businessId, IndustrySectors)
      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, taxYear, businessId, request.userType, mode))),
          value =>
            for {
              oldAnswers <- answersService.getAnswers[IndustrySectorsDb](ctx)
              newData <- answersService.replaceAnswers(
                ctx = ctx,
                data = oldAnswers
                  .getOrElse(IndustrySectorsDb())
                  .copy(isAllSelfEmploymentAbroad = Some(value))
              )
            } yield Redirect(navigator.nextPage(page, mode, newData, taxYear, businessId))
        )

  }

}
