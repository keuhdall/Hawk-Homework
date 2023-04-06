package types

import doobie.Read
import errors.{RankingValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Ranking.Ranking
import utils.*

object Ranking {
  final val RANKING_MIN_VALUE = 0
  final val RANKING_MAX_VALUE = 10

  opaque type Ranking = Int

  extension (ranking: Ranking) { def value: Int = ranking }

  def apply(value: Int): Ranking = value
  def safely(value: Int): Either[ValidationFailure, Ranking] =
    Either.cond(
      value >= RANKING_MIN_VALUE && value <= RANKING_MAX_VALUE,
      value,
      RankingValidationFailure
    )

  given (using read: Read[Int]): Read[Ranking] = read.map(identity)
}

given Encoder[Ranking] = Encoder[Int].contramap(_.value)
given Decoder[Ranking] = _.decoded(Ranking.safely)
