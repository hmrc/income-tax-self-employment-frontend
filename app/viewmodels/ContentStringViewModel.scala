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

package viewmodels

import pages.Page
import play.api.i18n.Messages

object ContentStringViewModel {

  def buildLegendHeadingWithHintString(headingText: String,
                                       hintText: String,
                                       headingClasses: String = "",
                                       hintExtraClasses: String = "")(implicit messages: Messages): String = {
    val heading           = messages(s"$headingText")
    val hint              = messages(s"$hintText")

    s"<div> <legend class='$headingClasses'> $heading </legend>" +
      s"<div class='govuk-hint $hintExtraClasses'> $hint </div> </div>"
  }

}
