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

package forms.standard

import forms.mappings.Mappings
import models.common.{Enumerable, UserType}
import pages.OneQuestionPage
import play.api.data.Form

import javax.inject.{Inject, Singleton}

@Singleton
class RadioButtonFormProvider @Inject() extends Mappings {

  def apply[A](page: OneQuestionPage[A], userType: UserType, altPrefix: Option[String] = None)(implicit `enum`: Enumerable[A]): Form[A] =
    Form("value" -> enumerable[A](userTypeAware(userType, altPrefix.fold(page.requiredErrorKey)(a => s"$a.error.required")))(enum))
}
