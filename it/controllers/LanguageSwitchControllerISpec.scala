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

package controllers

import base.IntegrationBaseSpec
import org.mockito.MockitoSugar
import play.api.test.Helpers._
import play.api.test.FakeRequest
import config.FrontendAppConfig
import models.common.TaxYear
import org.mockito.ArgumentMatchers.any
import org.scalatest.OptionValues
import play.api.i18n.Lang
import play.api.mvc.Headers

class LanguageSwitchControllerISpec extends IntegrationBaseSpec with OptionValues with MockitoSugar {

  private val english         = "english"
  private val welsh           = "cymraeg"
  private val other           = "french"
  private val fakeUrl: String = "/fakeUrl"

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]
  val requestHeaders: Headers          = new Headers(Seq(("Referer", fakeUrl)))

  private def switchLanguageRoute(lang: String): String = controllers.standard.routes.LanguageSwitchController.switchToLanguage(lang).url

  "LanguageSwitch Controller" when {
    "English selected" must {
      "switch to English" in {
        val request = FakeRequest(GET, switchLanguageRoute(english)).withHeaders(requestHeaders)

        val result = route(fakeApplication(), request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fakeUrl
        cookies(result).find(_.name == "PLAY_LANG").get.value mustEqual "en"
      }
    }

    "Other selected" must {
      "default to English" in {
        val request = FakeRequest(GET, switchLanguageRoute(other)).withHeaders(requestHeaders)

        val result = route(fakeApplication(), request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual fakeUrl
        cookies(result).find(_.name == "PLAY_LANG").get.value mustEqual "en"
      }
    }

    "no referer in header" must {
      "redirect to login continue url" in {
        when(mockAppConfig.languageMap).thenReturn(Map("en" -> Lang("en"), "cy" -> Lang("cy")))
        when(mockAppConfig.incomeTaxSubmissionStartUrl(any())).thenReturn(s"http://localhost:9304/update-and-submit-income-tax-return/$taxYear/start")

        val request = FakeRequest(GET, switchLanguageRoute(welsh))
        val result  = route(fakeApplication(), request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual mockAppConfig.incomeTaxSubmissionStartUrl(TaxYear.dateNow.getYear)
      }
    }
  }
}
