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

package controllers.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises

import cats.data.EitherT
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.{getMaxMonthsWithinTaxYearOrRedirect, handleResultT}
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesFormProvider
import forms.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesFormProvider.PeopleLivingAtBusinessPremisesFormModel
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.domain.BusinessData
import models.errors.ServiceError
import models.requests.DataRequest
import navigation.WorkplaceRunningCostsNavigator
import pages.expenses.workplaceRunningCosts.workingFromBusinessPremises._
import play.api.data.Form
import play.api.i18n._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.{BusinessService, SelfEmploymentService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.expenses.workplaceRunningCosts.workingFromBusinessPremises.PeopleLivingAtBusinessPremisesView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PeopleLivingAtBusinessPremisesController @Inject() (override val messagesApi: MessagesApi,
                                                          service: SelfEmploymentService,
                                                          businessService: BusinessService,
                                                          navigator: WorkplaceRunningCostsNavigator,
                                                          identify: IdentifierAction,
                                                          getData: DataRetrievalAction,
                                                          requireData: DataRequiredAction,
                                                          val controllerComponents: MessagesControllerComponents,
                                                          view: PeopleLivingAtBusinessPremisesView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val result = businessService.getBusiness(request.nino, businessId, request.mtditid) map { business =>
        getFilledFormAndMaxMonths(request, business, businessId, taxYear) match {
          case Left(redirectError) => redirectError
          case Right((filledForm: Form[PeopleLivingAtBusinessPremisesFormModel], maxMonths: Int)) =>
            Ok(view(filledForm, mode, request.userType, taxYear, businessId, maxMonths.toString))
        }
      }
      handleResultT(result)
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      def handleForm(maxMonths: Int): Future[Result] = {
        val formProvider = PeopleLivingAtBusinessPremisesFormProvider(request.userType, maxMonths)
        formProvider
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userType, taxYear, businessId, maxMonths.toString))),
            successfulForm => handleSuccess(successfulForm)
          )
      }
      def handleSuccess(form: PeopleLivingAtBusinessPremisesFormModel): Future[Result] =
        for {
          firstUpdated  <- service.persistAnswer(businessId, request.userAnswers, form.onePerson, LivingAtBusinessPremisesOnePerson)
          secondUpdated <- service.persistAnswer(businessId, firstUpdated, form.twoPeople, LivingAtBusinessPremisesTwoPeople)
          thirdUpdated  <- service.persistAnswer(businessId, secondUpdated, form.threePeople, LivingAtBusinessPremisesThreePlusPeople)
          result = Redirect(navigator.nextPage(PeopleLivingAtBusinessPremisesPage, mode, thirdUpdated, taxYear, businessId))
        } yield result

      val result: EitherT[Future, ServiceError, Result] =
        businessService.getBusiness(request.nino, businessId, request.mtditid) flatMap (business =>
          getMaxMonthsWithinTaxYearOrRedirect(business, taxYear) match {
            case Left(redirect: Result) => EitherT.right[ServiceError](Future.successful(redirect))
            case Right(maxMonths: Int)  => EitherT.right[ServiceError](handleForm(maxMonths))
          })

      handleResultT(result)
  }

  private def getFilledFormAndMaxMonths(request: DataRequest[_], business: BusinessData, businessId: BusinessId, taxYear: TaxYear)(implicit
      messages: Messages): Either[Result, (Form[PeopleLivingAtBusinessPremisesFormModel], Int)] =
    getMaxMonthsWithinTaxYearOrRedirect(business, taxYear) map { maxMonths =>
      val formProvider = PeopleLivingAtBusinessPremisesFormProvider(request.userType, maxMonths)
      val onePerson    = request.getValue(LivingAtBusinessPremisesOnePerson, businessId)
      val twoPeople    = request.getValue(LivingAtBusinessPremisesTwoPeople, businessId)
      val threePeople  = request.getValue(LivingAtBusinessPremisesThreePlusPeople, businessId)
      val filledForm: Form[PeopleLivingAtBusinessPremisesFormModel] = (onePerson, twoPeople, threePeople) match {
        case (Some(onePerson), Some(twoPeople), Some(threePeople)) =>
          formProvider.fill(PeopleLivingAtBusinessPremisesFormModel(onePerson, twoPeople, threePeople))
        case _ => formProvider
      }
      (filledForm, maxMonths)
    }

}
