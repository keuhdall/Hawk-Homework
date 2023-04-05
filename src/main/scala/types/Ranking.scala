package types

import cats.implicits.*
import doobie.Read
import errors.{RankingValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Ranking.Ranking

object Ranking {
  opaque type Ranking = Int

  extension (ranking: Ranking) { def value: Int = ranking }

  def apply(value: Int): Ranking = value
  def safely(value: Int): Either[ValidationFailure, Ranking] =
    Either.cond(value >= 0 && value <= 10, value, RankingValidationFailure)

  given (using read: Read[Int]): Read[Ranking] = read.map(identity)
}

given Encoder[Ranking] = Encoder[Int].contramap(_.value)
given Decoder[Ranking] = cursor =>
  for {
    value <- cursor.as[Int]
    ranking <- Ranking
      .safely(value)
      .leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode Ranking entity: ${e.getMessage}"),
          cursor
        )
      )
  } yield ranking
