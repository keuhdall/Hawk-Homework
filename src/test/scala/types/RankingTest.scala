package types

import cats.implicits.*
import errors.ValidationFailure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalacheck.Gen

class RankingTest extends AnyFlatSpec, Matchers, ScalaCheckPropertyChecks {

  "Ranking entity" should "fail for any value < 0" in {
    forAll(Gen.chooseNum(Int.MinValue, Ranking.RANKING_MIN_VALUE - 1))(n =>
      Ranking.safely(n).isLeft shouldBe true
    )
  }

  it should "fail for any value > 10" in {
    forAll(Gen.chooseNum(Ranking.RANKING_MAX_VALUE + 1, Int.MaxValue))(n =>
      Ranking.safely(n).isLeft shouldBe true
    )
  }

  it should "validate any ranking between 0 and 10" in {
    forAll(Gen.chooseNum(Ranking.RANKING_MIN_VALUE, Ranking.RANKING_MAX_VALUE))(
      n => Ranking.safely(n) shouldBe n.asRight[ValidationFailure]
    )
  }
}
