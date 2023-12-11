import wartremover.Wart

object WartRemoverSettings {

  lazy val warts: Seq[Wart] = Seq(
    Wart.ArrayEquals,
    Wart.Null
    // Wart.MutableDataStructures,
    // Not yet ready to enable it
    // Wart.ExplicitImplicitTypes,
    // Wart.OptionPartial,
    // Wart.Enumeration,
    // Wart.Throw,
    // Wart.LeakingSealed,
  )
}
