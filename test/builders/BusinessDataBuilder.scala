package builders

import models.requests.BusinessData

object BusinessDataBuilder {

  val aBusinessData1 = BusinessData(
    businessId = "id1",
    typeOfBusiness = "Name 1",
    tradingName = None,
    yearOfMigration = None,
    accountingPeriods = Seq.empty,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = None,
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = "example",
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = "example"
  )

  val aBusinessData2 = BusinessData(
    businessId = "id2",
    typeOfBusiness = "Name 2",
    tradingName = None,
    yearOfMigration = None,
    accountingPeriods = Seq.empty,
    firstAccountingPeriodStartDate = None,
    firstAccountingPeriodEndDate = None,
    latencyDetails = None,
    accountingType = None,
    commencementDate = None,
    cessationDate = None,
    businessAddressLineOne = "example",
    businessAddressLineTwo = None,
    businessAddressLineThree = None,
    businessAddressLineFour = None,
    businessAddressPostcode = None,
    businessAddressCountryCode = "example"
  )

}
