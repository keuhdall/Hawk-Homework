package types

import doobie.Read
import errors.{TitleValidationFailure, ValidationFailure}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Title.Title
import utils.*

object Title {
  final val MAX_TITLE_LENGTH = 150

  opaque type Title = String

  extension (title: Title) { def value: String = title }

  def apply(value: String): Title = value
  def safely(value: String): Either[ValidationFailure, Title] =
    Either.cond(value.length < MAX_TITLE_LENGTH, value, TitleValidationFailure)

  given (using read: Read[String]): Read[Title] = read.map(identity)
}

given Encoder[Title] = Encoder[String].contramap(_.value)
given Decoder[Title] = _.decoded(Title.safely)
