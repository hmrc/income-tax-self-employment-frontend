/*
 * Copyright 2025 HM Revenue & Customs
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

package views.helper

import org.jsoup.nodes.{Document, Element}
import play.twirl.api.TwirlHelperImports.twirlJavaCollectionToScala

import scala.jdk.CollectionConverters._

trait JsoupHelper {

  val main                 = "main"
  val paragraph: String    = s"$main p"
  val linkSelector: String = s"$main a"
  val li                   = "li"
  val ul                   = "ul"
  val ol                   = "ol"
  val h1: String           = "h1"
  val h2                   = s"$main h2"
  val h3                   = s"$main h3"
  val panelHeading         = s"$main div.govuk-panel.govuk-panel--confirmation $h1"
  val panelBody            = s"$main div.govuk-panel.govuk-panel--confirmation div.govuk-panel__body"

  val fieldset         = ".govuk-fieldset"
  val fieldsetLegend   = ".govuk-fieldset__legend"
  val legends: String  = s"$main div div div fieldset legend"
  val indent           = s"$main div.govuk-inset-text"
  val hint             = s"$main div.govuk-hint"
  val bullets: String  = "main ul.govuk-list.govuk-list--bullet li"
  val label            = s"$main label.govuk-label"
  val nthLabel: String = "form > div > div > label"
  val warning          = s"$main .govuk-warning-text__text"

  val contactHmrc               = "#contact-hmrc"
  val backLink                  = ".govuk-back-link"
  val button                    = ".govuk-button"
  val errorSummarySelector      = ".govuk-error-summary"
  val errorSummaryLinksSelector = s".govuk-error-summary__list a"
  val saveProgressButton        = ".govuk-button--secondary"
  val detailsSummary            = ".govuk-details__summary-text"
  val detailsContent            = ".govuk-details__text"
  val summaryListKey            = ".govuk-summary-list__key"
  val summaryListValue          = ".govuk-summary-list__value"
  val summaryListActions        = ".govuk-summary-list__actions"
  val hidden                    = ".hidden"
  val radio: String             = "div.govuk-radios__item label"

  case class Link(text: String, href: String)
  case class Details(summary: String, body: String)
  case class DateField(legend: String, hint: Option[String] = None)
  case class SummaryRow(label: String, answer: String, actions: Seq[Link])
  case class RadioGroup(legend: String, options: List[String], hint: Option[String] = None)

  implicit class SelectorDoc(doc: Document) {
    private def selectText(selector: String): List[String] =
      doc
        .select(selector)
        .toList
        .map(_.text())

    def heading: Option[String] =
      selectText(h1).headOption

    def headingLevel2(n: Int): Option[String] =
      selectText(h2).lift(n - 1)

    def headingLevel3(n: Int): Option[String] =
      selectText(h3).lift(n - 1)

    def hasBackLink: Boolean =
      doc.select(backLink).nonEmpty

    def errorSummary: Option[Element] =
      doc.select(errorSummarySelector).headOption

    def errorSummaryLinks: List[Link] =
      doc
        .select(errorSummaryLinksSelector)
        .toList
        .map(l => Link(l.text, l.attr("href")))

    def hasErrorSummary: Boolean = errorSummary.isDefined

    def summaryRow(n: Int): Option[SummaryRow] = {
      val key   = selectText(summaryListKey).lift(n - 1)
      val value = selectText(summaryListValue).lift(n - 1)
      val actions = doc
        .select(summaryListActions)
        .toList
        .lift(n)
        .map(
          _.select("a").toList
            .map(l => Link(l.text, l.attr("href")))
        )

      for {
        keyValue    <- key
        answerValue <- value
        actionsSeq  <- actions
      } yield SummaryRow(keyValue, answerValue, actionsSeq)
    }

    def hintWithMultiple(n: Int): Option[String] = doc.select(hint).toList.map(_.text).lift(n - 1)

    def paras: List[String] = doc.select(paragraph).toList.map(_.text)

    def para(n: Int): Option[String] = doc.select(paragraph).toList.map(_.text).lift(n - 1)

    def panelIndentHeading(n: Int): Option[String] = selectText(panelHeading).lift(n)

    def panelIndent(n: Int): Option[String] = selectText(indent).lift(n - 1)

    def unorderedList(n: Int): List[String] =
      doc
        .select(s"$main $ul")
        .toList
        .lift(n - 1)
        .map(_.children().eachText().asScala.toList)
        .getOrElse(throw new Exception("List element does not exist"))

    def legend(n: Int): Option[String] =
      doc
        .select(legends)
        .toList
        .map(_.text)
        .lift(n - 1)

    def bullet(n: Int): Option[String] =
      doc
        .select(bullets)
        .toList
        .map(_.text)
        .lift(n - 1)

    def orderedList(n: Int): List[String] =
      doc
        .select(s"$main $ol")
        .toList
        .lift(n - 1)
        .map(
          _.children()
            .eachText()
            .asScala
            .toList
        )
        .getOrElse(throw new Exception("List element does not exist"))

    def link(n: Int): Option[Link] =
      doc
        .select(linkSelector)
        .toList
        .map(l => Link(l.text, l.attr("href")))
        .lift(n - 1)

    def submitButton: Option[String] =
      doc
        .select(button)
        .headOption
        .map(_.text)

    def hintText: Option[String] =
      doc
        .select(hint)
        .headOption
        .map(_.text)

    def details: Option[Details] =
      doc.select(detailsSummary).headOption map { summary =>
        Details(summary.text, doc.select(detailsContent).first.text)
      }

    private def input(inputType: String, selector: String, selectorValue: String): Option[String] =
      doc.select(s"input[type=$inputType][$selector=$selectorValue]").headOption.map { elem =>
        doc.select(s"label[for=${elem.id}]").first.text
      }

    def dateInput(n: Int): Option[DateField] =
      doc.select(s"$main $fieldset").asScala.toList.lift(n - 1).map { elem =>
        DateField(
          legend = elem.select(fieldsetLegend).asScala.toList.head.text(),
          hint = elem.select(hint).asScala.toList.headOption.map(_.text)
        )
      }

    def radio(value: String): Option[String] = input("radio", "value", value)

    def radioGroup(n: Int): Option[RadioGroup] =
      doc.select(s"$main $fieldset").toList.lift(n - 1).map { elem =>
        RadioGroup(
          legend = elem.select("legend").first().text(),
          options = elem.select("label").toList.map(_.text()),
          hint = elem.select(".govuk-hint").toList.headOption.map(_.text)
        )
      }

    def checkbox(value: String): Option[String] = input("checkbox", "value", value)

    def textBox(id: String): Option[String] = input("text", "id", id)

    def textArea(id: String): Option[String] =
      doc.select(s"textarea[id=$id]").headOption.map { elem =>
        doc.select(s"label[for=${elem.id}]").first.text
      }

    def warningText(n: Int): Option[String] =
      doc.select(warning).toList.map(_.text).lift(n - 1)
  }

}
