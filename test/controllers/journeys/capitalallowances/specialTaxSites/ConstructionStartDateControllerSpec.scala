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

package controllers.journeys.capitalallowances.specialTaxSites

import base.questionPages.LocalDateGetAndPostQuestionBaseSpec
import models.NormalMode
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.specialTaxSites.{ConstructionStartDatePage, SpecialTaxSitesBasePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.{Call, Request}
import services.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesService
import views.html.journeys.capitalallowances.specialTaxSites.ConstructionStartDateView

import java.time.LocalDate

class ConstructionStartDateControllerSpec extends LocalDateGetAndPostQuestionBaseSpec("ConstructionStartDateController", ConstructionStartDatePage) {

  override def onPageLoadRoute: String = routes.ConstructionStartDateController.onPageLoad(taxYear, businessId, 0, NormalMode).url

  override def onSubmitRoute: String = routes.ConstructionStartDateController.onSubmit(taxYear, businessId, 0, NormalMode).url

  override def onwardRoute: Call = routes.QualifyingUseStartDateController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj("specialTaxSites" -> true, "newSpecialTaxSites" -> List(Json.obj("contractForBuildingConstruction" -> Some(false)))))

  override def pageAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "specialTaxSites"    -> true,
      "newSpecialTaxSites" -> List(Json.obj("contractForBuildingConstruction" -> Some(false), "constructionStartDate" -> Some(validDate.toString)))
    ))

  override def createForm(userType: UserType): Form[LocalDate] =
    form(page, userType, latestDateAndError = Some((LocalDate.of(2018, 10, 29), "constructionStartDate.error.tooEarly")))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
      request: Request[_],
      messages: Messages,
      application: Application): String = {
    val view = application.injector.instanceOf[ConstructionStartDateView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

  val mockStsService: SpecialTaxSitesService = mock[SpecialTaxSitesService]
  mockStsService.updateAndRedirectWithIndex(
    *[UserAnswers],
    *[LocalDate],
    *[BusinessId],
    *[TaxYear],
    *,
    *[SpecialTaxSitesBasePage[LocalDate]]) returns Redirect(onwardRoute).asFuture

}
