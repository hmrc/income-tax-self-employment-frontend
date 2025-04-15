package helpers

import base.IntegrationBaseSpec
import common.SessionValues
import play.api.Application
import play.api.libs.crypto.CookieSigner
import play.api.libs.ws.WSCookie
import play.api.mvc.AnyContentAsFormUrlEncoded
import play.api.test.FakeRequest
import uk.gov.hmrc.crypto.{Crypted, PlainText, SymmetricCryptoFactory}
import uk.gov.hmrc.http.SessionKeys

import java.net.{URLDecoder, URLEncoder}

trait SessionCookieHelper {
  self: IntegrationBaseSpec =>

  val cookieKey = "gvBoGdgzqG1AarzF1LY0zQ=="
  val sessionId = "session-ac4ed3e7-dbc3-4150-9574-40771c4285c1"

  def bakeSessionCookie(isAgent: Boolean, extraOptions: Option[Map[String, String]] = None): String =
    encodeCookieData(baseCookieValues(isAgent, extraOptions))

  def getCookieContents(cookie: WSCookie): Map[String, String] =
    getCookieData(cookie.value)

  def requestWithSession(req: FakeRequest[AnyContentAsFormUrlEncoded], userId: String): FakeRequest[AnyContentAsFormUrlEncoded] =
    req.withSession(
      SessionKeys.authToken -> "test",
      SessionKeys.sessionId -> sessionId
    )

  private def baseCookieValues(isAgent: Boolean, extraValues: Option[Map[String, String]] = None): Map[String, String] =
    Map(
      SessionKeys.authToken -> "test",
      SessionKeys.sessionId -> sessionId
    ) ++ {
      if (isAgent)
        Map(
          SessionValues.CLIENT_MTDITID -> mtditid.value,
          SessionValues.CLIENT_NINO    -> nino.value,
          SessionValues.TAX_YEAR       -> taxYear.endYear.toString
        )
      else
        Map(
          SessionValues.TAX_YEAR        -> taxYear.endYear.toString,
          SessionValues.VALID_TAX_YEARS -> Seq(taxYear.endYear.toString).mkString(",")
        )
    } ++ extraValues.getOrElse(Map.empty)

  private def encodeCookieData(sessionData: Map[String, String]): String = {
    def encode(data: Map[String, String]): PlainText = {
      val encoded = data
        .map { case (k, v) =>
          URLEncoder.encode(k, "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8")
        }
        .mkString("&")
      val key = "yNhI04vHs9<_HWbC`]20u`37=NGLGYY5:0Tg5?y`W<NoJnXWqmjcgZBec@rOxb^G".getBytes

      val cookieSignerCache: Application => CookieSigner = Application.instanceCache[CookieSigner]

      def cookieSigner: CookieSigner = cookieSignerCache(app)

      PlainText(cookieSigner.sign(encoded, key) + "-" + encoded)
    }

    val encodedCookie = encode(sessionData)
    val encrypted     = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).encrypt(encodedCookie).value

    s"""mdtp="$encrypted"; Path=/; HTTPOnly"; Path=/; HTTPOnly"""
  }

  private def getCookieData(cookieData: String): Map[String, String] = {
    val decrypted = SymmetricCryptoFactory.aesGcmCrypto(cookieKey).decrypt(Crypted(cookieData)).value
    val result = decrypted
      .split("&")
      .map(_.split("="))
      .map { case Array(k, v) => (k, URLDecoder.decode(v, java.nio.charset.Charset.defaultCharset().name())) }
      .toMap

    result
  }

}
