package utils

object StringOps {

  def lowerFirstLetter(s: String): String =
    if (s.isEmpty) s
    else s"${Character.toLowerCase(s.charAt(0))}${s.substring(1)}"
}
