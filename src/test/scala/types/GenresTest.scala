package types

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen
import types.Genres.GENRES_MAX_LENGTH

class GenresTest extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks {
  "Genres entity" should "fail for an empty list" in {
    Genres.safely(Nil).isLeft shouldBe true
  }

  it should "fail for any string of size greater than 50" in {
    val invalidSizeListGen =
      for {
        invalidSize <- Gen.chooseNum(GENRES_MAX_LENGTH + 1, 1000)
        string <- Gen
          .listOfN(invalidSize, Gen.asciiPrintableChar)
          .map(_.mkString)
        list <- Gen.nonEmptyListOf(string)
      } yield list
    forAll(invalidSizeListGen) { values =>
      Genres.safely(values).isLeft shouldBe true
    }
  }

  it should "validate any list of string of size <= 50, and return then as lowercase" in {
    val validListGen = Gen.nonEmptyListOf(
      Gen.listOfN(GENRES_MAX_LENGTH, Gen.asciiPrintableChar).map(_.mkString)
    )
    forAll(validListGen) { values =>
      Genres.safely(values) match {
        case Left(_)       => fail("expected Right, got Left")
        case Right(genres) => genres.value.forall(_.forall(_.isLower))
      }
    }
  }
}
