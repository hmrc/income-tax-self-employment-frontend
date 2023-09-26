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

package controllers

import base.SpecBase
import models.requests.BusinessData
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.SelfEmploymentDetailsViewModel
import views.html.CheckYourSelfEmploymentDetailsView

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, FormatStyle}

class CheckYourSelfEmploymentDetailsControllerSpec extends SpecBase {

  val taxYear: Int = LocalDate.now().getYear
  val businessId: String = "SJPR05893938418"
  val aBusinessData: BusinessData = BusinessData(
    businessId = "businessId", typeOfBusiness = "Carpenter", tradingName = Some("Alex Smith"), yearOfMigration = None,
    accountingPeriods = Seq.empty, firstAccountingPeriodStartDate = None, firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = Some("Traditional accounting (Accrual basis)"),
    commencementDate = Some(LocalDate.of(2022, 11, 14).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))),
    cessationDate = None,
    businessAddressLineOne = "TheAddress", businessAddressLineTwo = None, businessAddressLineThree = None,
    businessAddressLineFour = None, businessAddressPostcode = None, businessAddressCountryCode = "GB")

  val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
  val selfEmploymentDetails: SummaryList = SelfEmploymentDetailsViewModel.buildSummaryList(aBusinessData, isAgent = false)(messages(application))

  "CheckYourSelfEmploymentDetails Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourSelfEmploymentDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(selfEmploymentDetails, taxYear, "individual")(request, messages(application)).toString
        }
      }
    }
  }
}
