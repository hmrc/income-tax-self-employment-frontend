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

package controllers.journeys.capitalallowances.structuresBuildingsAllowance

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.standard.BooleanFormProvider
import models.common.{BusinessId, TaxYear}
import models.journeys.capitalallowances.structuresBuildingsAllowance.NewStructureBuilding
import pages.capitalallowances.structuresBuildingsAllowance.{NewStructuresBuildingsList, StructuresBuildingsRemovePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SelfEmploymentService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.journeys.capitalallowances.structuresBuildingsAllowance.StructuresBuildingsRemoveView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class StructuresBuildingsRemoveController @Inject() (override val messagesApi: MessagesApi,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: BooleanFormProvider,
                                                     service: SelfEmploymentService,
                                                     view: StructuresBuildingsRemoveView)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  private val page = StructuresBuildingsRemovePage
  def onPageLoad(taxYear: TaxYear, businessId: BusinessId, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(formProvider(page, request.userType), request.userType, taxYear, businessId, index))
  }

  def onSubmit(taxYear: TaxYear, businessId: BusinessId, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData) async {
    implicit request =>
      val redirect = Redirect(routes.StructuresBuildingsNewStructuresController.onPageLoad(taxYear, businessId))

      def handleSuccess(answer: Boolean): Future[Result] = {
        val structuresList = request.getValue(NewStructuresBuildingsList, businessId)
        val indexIsValid   = structuresList.exists(index >= 0 && index < _.length)
        (answer, indexIsValid, structuresList) match {
          case (true, true, Some(list)) => removeSiteAndRedirect(list)
          case (_, false, _) =>
            logger.error(s"Index '$index' is invalid in StructuresList of length '${structuresList.getOrElse(List.empty).length}")
            Future.successful(redirect)
          case _ => Future.successful(redirect)
        }
      }

      def removeSiteAndRedirect(structuresList: List[NewStructureBuilding]): Future[Result] = {
        val removeSiteFromList = structuresList.patch(index, Nil, 1)
        service.persistAnswer(businessId, request.userAnswers, removeSiteFromList, NewStructuresBuildingsList).map(_ => redirect)
      }

      formProvider(page, request.userType)
        .bindFromRequest()
        .fold(
          formErrors => Future.successful(BadRequest(view(formErrors, request.userType, taxYear, businessId, index))),
          answer => handleSuccess(answer)
        )
  }

}
