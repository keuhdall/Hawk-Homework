package types

import cats.data.Validated
import cats.implicits.*
import errors.{
  GenresSizeValidationFailure,
  GenresValuesValidationFailure,
  ValidationFailure
}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}

object Genres {

  opaque type Genres = List[String]

  extension (genres: Genres) { def value: List[String] = genres }

  def apply(value: List[String]): Validated[ValidationFailure, Genres] = {
    val invalidValues = value.filter(_.length > 50)
    value match {
      case Nil =>
        Validated.Invalid(GenresSizeValidationFailure)
      case _ if invalidValues.nonEmpty =>
        Validated.Invalid(GenresValuesValidationFailure(invalidValues))
      case _ =>
        Validated.Valid(value)
    }
  }

  given Encoder[Genres] = Encoder[List[String]]
  given Decoder[Genres] = cursor =>
    for {
      value <- cursor.as[List[String]]
      genres <- Genres(value).toEither.leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode Genres entity: ${e.getMessage}"),
          cursor
        )
      )
    } yield genres
}
