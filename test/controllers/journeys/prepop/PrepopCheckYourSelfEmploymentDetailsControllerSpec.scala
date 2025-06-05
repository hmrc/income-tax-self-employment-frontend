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

package controllers.journeys.prepop

import base.SpecBase
import cats.data.EitherT
import controllers.actions.AuthenticatedIdentifierAction.User
import controllers.journeys.prepop.routes._
import controllers.journeys.routes
import controllers.standard.routes._
import models.NormalMode
import models.common.BusinessId
import models.common.UserType.Individual
import models.domain.BusinessData
import models.errors.ServiceError.NotFoundError
import models.common.Journey.BusinessDetailsPrepop
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.SelfEmploymentService
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.language.LanguageUtils
import viewmodels.checkAnswers.prepop.PrepopSelfEmploymentDetailsViewModel
import views.html.journeys.prepop.PrepopCheckYourSelfEmploymentDetailsView

class PrepopCheckYourSelfEmploymentDetailsControllerSpec extends SpecBase with MockitoSugar {

  val nino = "AA370343B"
  val user = User(mtditid.value, None, nino, AffinityGroup.Individual.toString)

  val aBusinessData: BusinessData = BusinessData(
    businessId = "businessId",
    typeOfBusiness = "Carpenter",
    tradingName = Some("Alex Smith"),
    yearOfMigration = None,
    accountingPeriods = Seq.empty,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = Some("Traditional accounting (Accrual basis)"),
    commencementDate = Some("2022-11-14"),
    cessationDate = None,
    businessAddressLineOne = "TheAddress",
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = "GB"
  )

  val mockService: SelfEmploymentService = mock[SelfEmploymentService]

  "PrepopCheckYourSelfEmploymentDetails Controller" - {

    "onPageLoad" - {

      "must return OK with the correct view content" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .build()

        val languageUtils = application.injector.instanceOf[LanguageUtils]
        val selfEmploymentDetails =
          PrepopSelfEmploymentDetailsViewModel.buildSummaryList(aBusinessData, Individual, languageUtils)(messages(application))

        running(application) {
          val nextRoute = routes.SectionCompletedStateController.onPageLoad(taxYear, businessId, BusinessDetailsPrepop, NormalMode).url

          when(mockService.getBusiness(anyNino, anyBusinessId, anyMtditid)(any)) thenReturn EitherT.rightT(aBusinessData)

          val request = FakeRequest(GET, PrepopCheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, businessId).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PrepopCheckYourSelfEmploymentDetailsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(selfEmploymentDetails, taxYear, Individual, nextRoute)(request, messages(application)).toString
        }
      }

      "must redirect to the journey recovery page when an invalid business ID returns an error from the backend" in {

        val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[SelfEmploymentService].toInstance(mockService))
          .build()

        running(application) {
          val errorBusinessId: BusinessId = BusinessId("Bad BusinessID")

          when(mockService.getBusiness(anyNino, anyBusinessId, anyMtditid)(any)) thenReturn EitherT.leftT(
            NotFoundError(s"Unable to find business with ID: $businessId"))

          val request = FakeRequest(GET, PrepopCheckYourSelfEmploymentDetailsController.onPageLoad(taxYear, errorBusinessId).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
