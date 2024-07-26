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

package config

import base.SpecBase
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.Application
import play.api.i18n.MessagesApi

import scala.annotation.tailrec
import scala.io.Source
import scala.util.Using

class MessagesSpec extends SpecBase {

  lazy val app: Application         = applicationBuilder().build()
  lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  private val defaults              = messagesApi.messages("default")
  private val english               = messagesApi.messages("en")

  private val exclusionKeys: Set[String] = Set(
    "global.error.badRequest400.message",
    "global.error.pageNotFound404.message",
    "global.error.fallbackClientError4xx.heading",
    "global.error.fallbackClientError4xx.title",
    "language.day.plural",
    "language.day.singular"
  )

  /*  List of keys that repeat values of other keys (those other keys are NOT in this list)
      TODO merge identical values into one aptly-named key
   */
  private val exclusionKeysEn: Set[String] = Set(
    "continueClaimingAllowanceForExistingSite.title.agent",
    "wdaSpecialRateClaimAmount.l6.individual",
    "wdaSpecialRateClaimAmount.p4",
    "site.day",
    "date.month",
    "sectionCompletedState.yes",
    "structuresBuildingsPreviousClaimUse.title.agent",
    "structuresBuildingsQualifyingUseDate.subHeading",
    "qualifyingUseStartDate.change.hidden",
    "wdaMainRateClaimAmount.l4.agent",
    "writingDownAllowance.l1.agent",
    "wdaMainRateClaimAmount.details.individual",
    "wdaMainRateClaimAmount.p2.agent",
    "site.no",
    "structuresBuildingsQualifyingUseDate.title",
    "income.otherBusinessIncome",
    "wdaSpecialRateClaimAmount.details.individual",
    "wdaSpecialRateClaimAmount.l2.agent",
    "structuresBuildingsClaimedAmount.l2",
    "wdaMainRateClaimAmount.p5",
    "date.day",
    "doYouHaveAContinuingClaim.change.hidden",
    "structuresBuildingsQualifyingUseDate.hint.individual",
    "disallowableStaffCosts.l1.individual",
    "structuresBuildingsRemove.error.required.individual",
    "qualifyingUseStartDate.subHeading",
    "structuresBuildingsClaimedAmount.href",
    "wdaMainRateClaimAmount.l5.individual",
    "specialTaxSiteLocation.change.hidden",
    "existingSiteClaimingAmount.details.p3",
    "prepopTastList.hint",
    "wdaMainRateClaimAmount.l4.individual",
    "siteSummary.title.agent",
    "qualifyingUseStartDate.title",
    "structuresBuildingsPreviousClaimUse.p3.link",
    "wdaSpecialRateClaimAmount.l6.agent",
    "structuresBuildingsClaimedAmount.p3",
    "structuresBuildingsQualifyingUseDate.p1",
    "structuresBuildingsPreviousClaimUse.p3.href",
    "wdaMainRateClaimAmount.l1",
    "structuresBuildingsPreviousClaimUse.title.individual",
    "wdaMainRateClaimAmount.l2.agent",
    "siteSummary.title.individual",
    "wdaSpecialRateClaimAmount.p6",
    "continueClaimingAllowanceForExistingSite.title.individual",
    "newTaxSites.remove.hidden",
    "zegvAllowance.p2.individual",
    "wdaMainRateClaimAmount.l5.agent",
    "wdaSpecialRateClaimAmount.p2",
    "site.yes",
    "existingSiteClaimingAmount.details.l2",
    "wdaSpecialRateClaimAmount.l3.individual",
    "zecOnlyForSelfEmployment.no.agent",
    "structuresBuildingsRemove.error.required.agent",
    "site.month",
    "structuresBuildingsEligibleClaim.change.hidden",
    "qualifyingUseStartDate.error.tooLate",
    "structuresBuildingsPreviousClaimUse.hint.individual",
    "wdaSpecialRateClaimAmount.l5.individual",
    "wdaSpecialRateClaimAmount.heading",
    "specialTaxSites.details.l5.href",
    "structuresBuildingsEligibleClaim.l3.link",
    "zecHowMuchDoYouWantToClaim.change.hidden",
    "wdaSpecialRateClaimAmount.l1",
    "structuresBuildingsEligibleClaim.hint.individual",
    "zegvHowMuchDoYouWantToClaim.change.hidden",
    "structuresBuildingsLocation.error.postcode.agent",
    "structuresBuildingsClaimedAmount.title",
    "zecOnlyForSelfEmployment.no.individual",
    "structuresBuildingsClaimedAmount.l1",
    "qualifyingUseStartDate.hint.individual",
    "removeSpecialTaxSite.error.required.individual",
    "wdaMainRateClaimAmount.p3",
    "common.opensInNewTab",
    "zegvOnlyForSelfEmployment.no.individual",
    "qualifyingUseStartDate.error.required",
    "existingSiteClaimingAmount.title",
    "wdaSpecialRateClaimAmount.details.agent",
    "wdaMainRateClaimAmount.p1.individual",
    "existingSiteClaimingAmount.details.l1",
    "writingDownAllowance.l1.individual",
    "wdaMainRateClaimAmount.p4.individual",
    "wdaMainRateClaimAmount.p2.individual",
    "newSiteClaimingAmount.change.hidden",
    "newSpecialTaxSites.change.hidden",
    "wdaSpecialRateClaimAmount.p3.individual",
    "specialTaxSites.details.l5.link",
    "structuresBuildingsEligibleClaim.l3.href",
    "removeSpecialTaxSite.error.required.agent",
    "expenses.contributions.individual",
    "date.year",
    "wdaMainRateClaimAmount.l6.agent",
    "wdaSpecialRateClaimAmount.l2.individual",
    "wdaMainRateClaimAmount.l6.individual",
    "wdaMainRateClaimAmount.heading",
    "continueClaimingAllowanceForExistingSite.change.hidden",
    "wdaMainRateClaimAmount.l2.individual",
    "wdaSpecialRateClaimAmount.p5.agent",
    "wdaSpecialRateClaimAmount.l4.individual",
    "qualifyingUseStartDate.p1",
    "zecHowMuchDoYouWantToClaim.error.required.individual",
    "wdaSpecialRateClaimAmount.l5.agent",
    "wdaSpecialRateClaimAmount.l4.agent",
    "balancingAllowance.p7.tab",
    "structuresBuildingsLocation.error.postcode.individual",
    "sectionCompletedState.no",
    "zegvHowMuchDoYouWantToClaim.error.required.individual",
    "newTaxSites.change.hidden",
    "zegvOnlyForSelfEmployment.no.agent",
    "structuresBuildingsQualifyingUseDate.error",
    "capitalAllowance.fullCost",
    "adjustments.outstandingBusinessIncome",
    "wdaSpecialRateClaimAmount.p5.individual",
    "site.year",
    "wdaMainRateClaimAmount.details.agent",
    "zegvHowMuchDoYouWantToClaim.fullCost",
    "capitalAllowance.useFirstYearAllowance",
    "structuresBuildingsEligibleClaim.href",
    "wdaMainRateClaimAmount.l3",
    "structuresBuildingsQualifyingUseDate.error.inFuture",
    "prepop.useReportingSoftware",
    "wdaMainRateClaimAmount.p4.agent"
  )

  private val exclusionKeySubstrings: Set[String] = Set(
    "checkYourAnswersLabel.individual",
    "journeys",
    ".cya"
  )

  private val illegalCharacters: Set[Char] = Set('\'', '`')

  // TODO Go through this list and fix content for agent
  private val userSpecificMessagesWithoutAgentVersions = List(
    "amountSpentOnEvcp.title.cya",
    "checkYourSelfEmploymentDetails.accountingType",
    "expenses.cyaSummary.agent",
    "expenses.hint.disallowableExpenses",
    "expensesCategories.p1.agent",
    "expensesCategories.p2.agent",
    "expensesCategories.p3",
    "expensesCategories.p4.agent",
    "howMuchTradingAllowance.p1.agent",
    "howMuchTradingAllowance.p2",
    "howMuchTradingAllowance.subHeading.agent",
    "incomeNotCountedAsTurnover.p2",
    "peopleLivingAtBusinessPremises.p1.agent",
    "sectionCompletedState.title",
    "selectCapitalAllowances.subText.annualInvestment",
    "selectCapitalAllowances.subText.balancing",
    "selectCapitalAllowances.subText.balancingCharge.ACCRUAL",
    "selectCapitalAllowances.subText.balancingCharge.CASH",
    "selectCapitalAllowances.subText.electricVehicleChargepoint",
    "selectCapitalAllowances.subText.structuresAndBuildings",
    "selectCapitalAllowances.subText.writingDown",
    "selectCapitalAllowances.subText.zeroEmissionCar.ACCRUAL",
    "selectCapitalAllowances.subText.zeroEmissionCar.CASH",
    "selectCapitalAllowances.subText.zeroEmissionGoodsVehicle",
    "signedOut.title",
    "specialTaxSites.details.heading",
    "structuresBuildingsAllowance.p1.agent",
    "taxReliefChargePoints.hint.agent",
    "timeout.message",
    "turnoverNotTaxable.p1.agent",
    "turnoverNotTaxable.p3.agent",
    "wfhFlatRateOrActualCosts.error.required.agent",
    "wfhFlatRateOrActualCosts.subHeading.agent",
    "newTaxSites.emptyList.agent",
    "goodsAndServicesForYourOwnUse.hint.agent",
    "checkNetProfitLoss.p1.profit.agent",
    "checkNetProfitLoss.p1.loss.agent"
  )

  "messages must not contain any illegal characters" in {
    for {
      char <- illegalCharacters
      key  <- english.values.toList
    } key should not contain char
  }

  "there should be no duplicate messages(values) in the" - {
    "default messages" in {
      val messages: List[(String, String)] = filterExcludedKeys(defaults.toList, exclusionKeys, exclusionKeySubstrings)

      val result = checkMessagesAreUnique(messages, messages)

      result mustBe Set()
    }

    "english messages file" in {
      val messages: List[(String, String)] = filterExcludedKeys(english.toList, exclusionKeysEn, exclusionKeySubstrings)

      val result = checkMessagesAreUnique(messages, messages)

      result mustBe Set()
    }

    "no duplicate properties" in {
      val filePath = "conf/messages.en"

      val keys = Using(Source.fromFile(filePath)) { source =>
        source
          .getLines()
          .filter(_.contains("="))
          .toList
          .map(_.takeWhile(_ != '=').trim)
      }.success.value

      val duplicateKeys = keys
        .groupBy(identity)
        .view
        .mapValues(_.size)
        .collect {
          case (property, count) if count > 1 =>
            property
        }
        .toList
      duplicateKeys mustBe Nil
    }
  }

  "config.MessagesSpec" - {

    val exampleMessages = List(
      ("key1.example`1", "this is the example text"),
      ("key2.example%2", "this is also example text"),
      ("key''3.example3", "this is more example text")
    )
    val repeatedMessages = exampleMessages ++ List(
      ("excludedKey.excludedExample", "this is the example text"),
      ("excludedSubKey.uniqueSubKey", "this is the example text"))
    val exclusionKeys     = Set("excludedKey.excludedExample")
    val exclusionSubKeys  = Set("excludedSubKey")
    val illegalCharacters = Set("'", "%", "`")

    "filterExcludedKeys" - {
      "should return only messages from the list that aren't excluded by their key or sub key" in {
        filterExcludedKeys(repeatedMessages, exclusionKeys, exclusionSubKeys) mustBe exampleMessages
      }
    }

    "checkMessagesAreUnique" - {
      "should return an empty set when" - {
        "messages are all unique" in {
          val result = checkMessagesAreUnique(exampleMessages, exampleMessages)

          result mustBe Set()
        }
        "any messages that aren't unique are excluded by the exclusionKeys or exclusionSubKeys" in {
          val testMessages: List[(String, String)] = filterExcludedKeys(repeatedMessages, exclusionKeys, exclusionSubKeys)

          val result = checkMessagesAreUnique(testMessages, testMessages)

          result mustBe Set()
        }
      }

      "should return a set of failed message keys when" - {
        "there are repeated messages that aren't excluded" in {
          val testMessages: List[(String, String)] = filterExcludedKeys(
            repeatedMessages,
            exclusionKeys = Set("excludedKey.differentExample", "excludedSubKey"),
            exclusionSubKeys = Set("key3"))

          val result = checkMessagesAreUnique(testMessages, testMessages)

          result mustBe Set("key1.example`1", "excludedSubKey.uniqueSubKey", "excludedKey.excludedExample")
        }
      }
    }

    "checkForIllegalCharacters" - {
      "should return an empty set when messages contains no illegal characters" in {
        val badMessages = exampleMessages ++ List(("example4", "apostrophe'"), ("example5", "back`tick"))
        val result1     = checkForIllegalCharacters(exampleMessages, illegalCharacters)
        val result2     = checkForIllegalCharacters(badMessages, Set.empty)

        result1 mustBe Set()
        result2 mustBe Set()
      }

      "should return a set of failed message keys when their values contain an illegal character" in {
        val badMessages = exampleMessages ++ List(("example4", "apostrophe'"), ("example5", "back`tick"))
        val result      = checkForIllegalCharacters(badMessages, illegalCharacters)

        result mustBe Set("example4", "example5")
      }
    }

    "make sure user specific messages have also agent version" in {
      val missingAgentVersionMessages = english.toList
        .filterNot(_._1.endsWith("hidden"))
        .filterNot { case (msgKey, _) =>
          userSpecificMessagesWithoutAgentVersions.contains(msgKey)
        }
        .filter { case (msgKey, msgValue) =>
          msgValue.toLowerCase.contains(" you ") && !msgKey.contains(".individual")
        }
        .sortBy(_._1)

      missingAgentVersionMessages.foreach { case (msgKey, msgValue) =>
        println(s"$msgKey=$msgValue")
      }

      val hasNoMissingAgentVersionMessageValues = missingAgentVersionMessages.isEmpty
      assert(
        hasNoMissingAgentVersionMessageValues,
        "Perhaps missing the agent version of the message. Add to the userSpecificMessagesWithoutAgentVersionsexception list if not true. " +
          "See the console for details which keys are affected."
      )

    }

  }

  private def filterExcludedKeys(messages: List[(String, String)], exclusionKeys: Set[String], exclusionSubKeys: Set[String]) =
    messages.filter { entry =>
      !exclusionKeys.contains(entry._1) && !exclusionSubKeys.exists(entry._1.contains(_))
    }

  @tailrec
  private def checkMessagesAreUnique(keysToTest: List[(String, String)],
                                     remaining: List[(String, String)],
                                     result: Set[String] = Set.empty): Set[String] =
    remaining match {
      case Nil => result
      case (currentKey, currentMessage) :: tail =>
        val duplicate = keysToTest.collect {
          case (messageKey, message) if currentMessage == message && currentKey != messageKey =>
            currentKey
        }.toSet
        checkMessagesAreUnique(keysToTest, tail, duplicate ++ result)
    }

  @tailrec
  private def checkForIllegalCharacters(remaining: List[(String, String)],
                                        illegalCharacters: Set[String],
                                        result: Set[String] = Set.empty): Set[String] =
    remaining match {
      case Nil => result
      case (key, value) :: tail =>
        val containsForbiddenChar = illegalCharacters.exists(value.contains(_))
        if (containsForbiddenChar) {
          checkForIllegalCharacters(tail, illegalCharacters, result + key)
        } else {
          checkForIllegalCharacters(tail, illegalCharacters, result)
        }
    }

}
