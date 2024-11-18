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

import base.questionPages.BigDecimalGetAndPostQuestionBaseSpec
import cats.implicits.catsSyntaxOptionId
import models.NormalMode
import models.common.{BusinessId, UserType}
import models.database.UserAnswers
import models.journeys.capitalallowances.specialTaxSites.SpecialTaxSiteLocation
import org.mockito.IdiomaticMockito.StubbingOps
import pages.capitalallowances.specialTaxSites.{QualifyingExpenditurePage, SpecialTaxSitesBasePage}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.{Call, Request}
import services.journeys.capitalallowances.specialTaxSites.SpecialTaxSitesService
import views.html.journeys.capitalallowances.specialTaxSites.QualifyingExpenditureView

class QualifyingExpenditureControllerSpec
    extends BigDecimalGetAndPostQuestionBaseSpec("QualifyingExpenditureController", QualifyingExpenditurePage) {

  override def onPageLoadRoute: String = routes.QualifyingExpenditureController.onPageLoad(taxYear, businessId, 0, NormalMode).url

  override def onSubmitRoute: String = routes.QualifyingExpenditureController.onSubmit(taxYear, businessId, 0, NormalMode).url

  override def onwardRoute: Call = routes.SpecialTaxSiteLocationController.onPageLoad(taxYear, businessId, 0, NormalMode)

  override def createForm(userType: UserType): Form[BigDecimal] = form(page, userType)


  override def baseAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "specialTaxSites" -> true,
      "newSpecialTaxSites" -> List(Json.obj(
        "contractForBuildingConstruction" -> Some(false),
        "constructionStartDate"           -> Some("2022-03-02"),
        "qualifyingUseStartDate"          -> Some("2022-03-02")
      ))
    ))

  override def pageAnswers: UserAnswers = buildUserAnswers(
    Json.obj(
      "specialTaxSites" -> true,
      "newSpecialTaxSites" -> List(Json.obj(
        "contractForBuildingConstruction" -> Some(false),
        "constructionStartDate"           -> Some("2022-03-02"),
        "qualifyingUseStartDate"          -> Some("2022-03-02"),
        "qualifyingExpenditure"           -> Some(amount)
      ))
    ))

  override def expectedView(form: Form[_], scenario: TestScenario)(implicit
                                                                   request: Request[_],
                                                                   messages: Messages,
                                                                   application: Application): String = {
    val view = application.injector.instanceOf[QualifyingExpenditureView]
    view(form, scenario.mode, scenario.userType, scenario.taxYear, scenario.businessId, 0).toString()
  }

  val mockStsService: SpecialTaxSitesService = mock[SpecialTaxSitesService]
  mockStsService.updateSiteAnswerWithIndex(
    *[UserAnswers],
    *[BigDecimal],
    *[BusinessId],
    *,
    *[SpecialTaxSitesBasePage[BigDecimal]]) returns pageAnswers
    .set(page, amount, businessId.some)
    .success
    .value
    .asFuture

}
