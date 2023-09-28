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
import connectors.SelfEmploymentConnector
import controllers.actions.AuthenticatedIdentifierAction.User
import models.errors.HttpError
import models.errors.HttpErrorBody.SingleErrorBody
import models.mdtp.BusinessData
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup
import viewmodels.checkAnswers.SelfEmploymentDetailsViewModel
import views.html.CheckYourSelfEmploymentDetailsView

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CheckYourSelfEmploymentDetailsControllerSpec extends SpecBase with MockitoSugar {

  val taxYear = LocalDate.now().getYear
  val nino = "AA370343B"
  val mtditid = "mtditid"
  val user = User(mtditid, None, nino, AffinityGroup.Individual.toString)

  val aBusinessData: BusinessData = BusinessData(
    businessId = "businessId", typeOfBusiness = "Carpenter", tradingName = Some("Alex Smith"), yearOfMigration = None,
    accountingPeriods = Seq.empty, firstAccountingPeriodStartDate = None, firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = Some("Traditional accounting (Accrual basis)"),
    commencementDate = Some("2022-11-14"),
    cessationDate = None,
    businessAddressLineOne = "TheAddress", businessAddressLineTwo = None, businessAddressLineThree = None,
    businessAddressLineFour = None, businessAddressPostcode = None, businessAddressCountryCode = "GB")

  implicit val ec: ExecutionContext = ExecutionContext.global

  val mockConnector: SelfEmploymentConnector = mock[SelfEmploymentConnector]

  "CheckYourSelfEmploymentDetails Controller" - {

    "onPageLoad" - {

      "must return OK with the correct view content" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector)).build()

        val selfEmploymentDetails = SelfEmploymentDetailsViewModel.buildSummaryList(aBusinessData, isAgent = false)(messages(application))

        running(application) {
          val businessId: String = "SJPR05893938418"

          when(mockConnector.getBusiness(any, meq(businessId), any)(any, any)) thenReturn Future(Right(Seq(aBusinessData)))

          val request = FakeRequest(GET, routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourSelfEmploymentDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(selfEmploymentDetails, taxYear, "individual")(request, messages(application)).toString
        }
      }

      "must redirect to the journey recovery page when an invalid business ID returns an error from the backend" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentConnector].toInstance(mockConnector)).build()

        running(application) {
          val errorBusinessId: String = "Bad BusinessID"

          when(mockConnector.getBusiness(any, meq(errorBusinessId), any)(any, any)
          ) thenReturn Future(Left(HttpError(BAD_REQUEST, SingleErrorBody("404", "BusinessID not found"))))

          val request = FakeRequest(GET, routes.CheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, errorBusinessId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
