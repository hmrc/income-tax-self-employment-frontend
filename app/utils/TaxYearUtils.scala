package utils

import java.time.{LocalDate, Month}

object TaxYearUtils {

  val dateNow = LocalDate.now()
  val currentTaxYearStartDate =
    if (dateNow.isBefore(LocalDate.of(dateNow.getYear, Month.APRIL, 6))) LocalDate.of(dateNow.getYear - 1, Month.APRIL, 6)
    else LocalDate.of(dateNow.getYear, Month.APRIL, 6)

}
