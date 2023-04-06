package types

import cats.implicits.*
import errors.ValidationFailure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen
import types.Country.VALID_COUNTRY_LENGTH

class CountryTest extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks {

  "Country entity" should "fail for any string with a length != 3" in {
    forAll(Gen.alphaUpperStr.suchThat(_.length != VALID_COUNTRY_LENGTH)) {
      str => Country.safely(str).isLeft shouldBe true
    }
  }

  it should "fail for any non-alpha string of a valid size" in {
    val validSizeStrGen =
      Gen
        .listOfN(VALID_COUNTRY_LENGTH, Gen.asciiPrintableChar)
        .map(_.mkString.toUpperCase)
    forAll(validSizeStrGen.suchThat(s => !s.forall(_.isLetter)))(str =>
      Country.safely(str).isLeft shouldBe true
    )
  }

  it should "fail for any alpha string of valid size but not fully-uppercase" in {
    val validSizeAlphaStrGen =
      Gen.listOfN(VALID_COUNTRY_LENGTH, Gen.alphaChar).map(_.mkString)
    forAll(validSizeAlphaStrGen.suchThat(s => !s.forall(_.isUpper)))(str =>
      Country.safely(str).isLeft shouldBe true
    )
  }

  /*
   * NOTE: this code is actually accurate:
   * I am not discriminating on country name, but rather checking some properties
   * of the ISO 3166-1 alpha-3 norm (length, uppercase...)
   * For proper checking I could either have created my own ADT or used this lib:
   * https://github.com/vitorsvieira/scala-iso
   * but in order to keep the code simple I just made some simpler checks
   */
  it should "validate any ISO 3166-1 alpha-3 country" in {
    val isoCountryGen =
      Gen.listOfN(VALID_COUNTRY_LENGTH, Gen.alphaUpperChar).map(_.mkString)
    forAll(isoCountryGen)(str =>
      Country.safely(str) shouldBe str.asRight[ValidationFailure]
    )
  }
}
