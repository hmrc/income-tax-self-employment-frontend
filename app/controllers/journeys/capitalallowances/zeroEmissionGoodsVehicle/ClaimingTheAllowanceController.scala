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

package controllers.journeys.capitalallowances.zeroEmissionGoodsVehicle

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.capitalallowances.zeroEmissionGoodsVehicle.ClaimingTheAllowanceFormProvider
import models.Mode
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.zeroEmissionGoodsVehicle.{ClaimingTheAllowance, ZeroEmissionGoodsVehicleAllowance}
import pages.capitalallowances.zeroEmissionGoodsVehicle._
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.zeroEmissionGoodsVehicle.ClaimingTheAllowanceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ClaimingTheAllowanceController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: ClaimingTheAllowanceFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: ClaimingTheAllowanceView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val form: Form[ClaimingTheAllowance] = request.fillForm(
        ClaimingTheAllowancePage,
        businessId,
        formProvider(request.userType, taxYear)
      )

      Ok(view(form, mode, request.userType, taxYear, businessId))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      ClaimingTheAllowancePage.redirectNextPage(mode)(request.userAnswers, businessId, taxYear)
    }
}
