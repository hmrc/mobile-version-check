package uk.gov.hmrc.mobileversioncheck.stubs

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, stubFor, urlPathEqualTo}

object CustomerProfileStub {
  def upgradeRequired( upgrade: Boolean ): Unit =
    stubFor(post(urlPathEqualTo(s"/profile/native-app/version-check")).willReturn(
      aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(
        s"""{ "upgrade": $upgrade }""")))
}
