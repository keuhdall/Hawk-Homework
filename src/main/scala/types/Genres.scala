package types

import cats.implicits.catsSyntaxEitherId
import doobie.Read
import errors.{
  GenresSizeValidationFailure,
  GenresValuesValidationFailure,
  ValidationFailure
}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Genres.Genres
import utils.*

object Genres {
  final val GENRES_MAX_LENGTH = 50

  opaque type Genres = List[String]

  extension (genres: Genres) { def value: List[String] = genres }

  def apply(value: List[String]): Genres = value.map(_.toLowerCase)
  def safely(value: List[String]): Either[ValidationFailure, Genres] = {
    val invalidValues = value.filter(_.length > GENRES_MAX_LENGTH)
    value match {
      case Nil => GenresSizeValidationFailure.asLeft
      case _ if invalidValues.nonEmpty =>
        GenresValuesValidationFailure(invalidValues).asLeft
      case _ => value.map(_.toLowerCase).asRight
    }
  }

  given (using read: Read[List[String]]): Read[Genres] = read.map(identity)
}

given Encoder[Genres] = Encoder[List[String]].contramap(_.value)
given Decoder[Genres] = _.decoded(Genres.safely)
