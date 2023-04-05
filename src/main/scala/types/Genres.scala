package types

import cats.implicits.*
import doobie.Read
import errors.{
  GenresSizeValidationFailure,
  GenresValuesValidationFailure,
  ValidationFailure
}
import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder}
import types.Genres.Genres

object Genres {
  opaque type Genres = List[String]

  extension (genres: Genres) { def value: List[String] = genres }

  def apply(value: List[String]): Genres = value
  def safely(value: List[String]): Either[ValidationFailure, Genres] = {
    val invalidValues = value.filter(_.length > 50)
    value match {
      case Nil => GenresSizeValidationFailure.asLeft
      case _ if invalidValues.nonEmpty =>
        GenresValuesValidationFailure(invalidValues).asLeft
      case _ => value.asRight
    }
  }

  given (using read: Read[List[String]]): Read[Genres] = read.map(identity)
}

given Encoder[Genres] = Encoder[List[String]].contramap(_.value)
given Decoder[Genres] = cursor =>
  for {
    value <- cursor.as[List[String]]
    genres <- Genres
      .safely(value)
      .leftMap(e =>
        DecodingFailure(
          CustomReason(s"failed to decode Genres entity: ${e.getMessage}"),
          cursor
        )
      )
  } yield genres
