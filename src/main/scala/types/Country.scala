package types

import cats.data.Validated
import cats.implicits.*
import errors.{CountryValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}

object Country {

  opaque type Country = String

  extension (country: Country) { def value: String = country }

  def apply(value: String): Validated[ValidationFailure, Country] =
    Validated.cond(value.length == 3, value, CountryValidationFailure)

  given Encoder[Country] = Encoder[String]
  given Decoder[Country] = cursor =>
    for {
      value <- cursor.as[String]
      country <- Country(value).toEither.leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode Country entity: ${e.getMessage}"),
          cursor
        )
      )
    } yield country
}
