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

package forms.nics

import forms.FormSpec
import models.common.UserType.Individual
import pages.nics.Class4DivingExemptPage
import play.api.data.FormError

class Class4ExemptBusinessesFormProviderSpec extends FormSpec {

  def mapping(value: AnyVal): Map[String, String] = Map("value[0]" -> value.toString)
  val form                                        = new Class4ExemptBusinessesFormProvider()(Class4DivingExemptPage, Individual)

  "FormProvider should" - {
    "bind valid value" in {
      val valid = mapping(businessId)

      form.bind(valid).get mustEqual List(businessId)
    }

    "fail to bind invalid value" in {
      val invalid = mapping(nino)

      form.bind(invalid).get must not equal List(nino)
    }

    "fail to bind when no answer is selected" in {
      val emptyMapping = Map.empty[String, String]

      form.bind(emptyMapping).errors must contain(FormError("value", s"class4DivingExempt.error.required.$Individual"))
    }
  }
}
