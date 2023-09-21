package models

sealed trait Journey
case object TradeDetails extends Journey {
  override def toString: String = "trade-details"
}

case object Income extends Journey {
  override def toString: String = "income"
}
