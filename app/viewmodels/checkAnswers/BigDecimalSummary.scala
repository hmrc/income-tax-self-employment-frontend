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

package viewmodels.checkAnswers

import cats.implicits.catsSyntaxOptionId
import models.common.{BusinessId, TaxYear, UserType}
import models.database.UserAnswers
import pages.OneQuestionPage
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class BigDecimalSummary(page: OneQuestionPage[BigDecimal], callLink: Call) extends AnswerSummary {

  def row(answers: UserAnswers,
          taxYear: TaxYear,
          businessId: BusinessId,
          userType: UserType,
          rightTextAlign: Boolean = true,
          overrideKeyMessage: Option[String] = None,
          overrideChangeMessage: Option[String] = None)(implicit messages: Messages): Option[SummaryListRow] =
    answers
      .get(page, businessId.some)
      .map(answer => mkBigDecimalSummary(answer, callLink, page, userType, rightTextAlign, overrideKeyMessage, overrideChangeMessage))
}
