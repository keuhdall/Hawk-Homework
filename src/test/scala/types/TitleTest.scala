package types

import cats.implicits.*
import errors.ValidationFailure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen

class TitleTest extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks {

  "Title entity" should "fail for any string with a length > 250" in {
    val invalidSizeStrGen = for {
      invalidSize <- Gen.chooseNum(Title.MAX_TITLE_LENGTH + 1, 1000)
      string <- Gen.listOfN(invalidSize, Gen.asciiPrintableChar).map(_.mkString)
    } yield string

    forAll(invalidSizeStrGen)(str => Title.safely(str).isLeft shouldBe true)
  }

  it should "validate any string of size <= 250" in {
    forAll(
      Gen
        .listOfN(Title.MAX_TITLE_LENGTH, Gen.asciiPrintableChar)
        .map(_.mkString)
    )(str => Title.safely(str) shouldBe str.asRight[ValidationFailure])
  }
}
