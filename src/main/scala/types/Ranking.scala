package types

import cats.data.Validated
import cats.implicits.*
import errors.{RankingValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}

object Ranking {

  opaque type Ranking = Int

  extension (ranking: Ranking) { def value: Int = ranking }

  def apply(value: Int): Validated[ValidationFailure, Ranking] =
    Validated.cond(value >= 0 && value <= 10, value, RankingValidationFailure)

  given Encoder[Ranking] = Encoder[Int]
  given Decoder[Ranking] = cursor =>
    for {
      value <- cursor.as[Int]
      ranking <- Ranking(value).toEither.leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode Ranking entity: ${e.getMessage}"),
          cursor
        )
      )
    } yield ranking
}
