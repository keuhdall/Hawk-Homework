package utils

import cats.implicits.catsSyntaxEither
import errors.ValidationFailure
import io.circe.Decoder.Result
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, HCursor}

extension [A, B](cursor: HCursor) {
  def decoded(
      f: A => Either[ValidationFailure, B]
  )(using Decoder[A]): Result[B] =
    for {
      value <- cursor.as[A]
      entity <- f(value).leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode entity: ${e.getMessage}"),
          cursor
        )
      )
    } yield entity
}
