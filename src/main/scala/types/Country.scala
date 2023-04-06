package types

import doobie.Read
import errors.{CountryValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Country.Country
import utils.*

object Country {
  final val VALID_COUNTRY_LENGTH = 3

  opaque type Country = String

  extension (country: Country) { def value: String = country }

  def apply(value: String): Country = value
  def safely(value: String): Either[ValidationFailure, Country] =
    Either.cond(
      value.length == VALID_COUNTRY_LENGTH &&
        value.forall(c => c.isLetter && c.isUpper),
      value,
      CountryValidationFailure
    )

  given (using read: Read[String]): Read[Country] = read.map(identity)
}

given Encoder[Country] = Encoder[String].contramap(_.value)
given Decoder[Country] = _.decoded(Country.safely)
