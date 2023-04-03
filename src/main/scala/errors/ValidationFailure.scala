package errors

trait ValidationFailure extends Throwable

case object CountryValidationFailure extends ValidationFailure {
  override def getMessage: String =
    "Invalid country value: should be a valid ISO 3166-1 alpha-3"
}

case object GenresSizeValidationFailure extends ValidationFailure {
  override def getMessage: String =
    "Invalid size for genres: should be at least 1"
}

case class GenresValuesValidationFailure(invalidValues: List[String])
    extends ValidationFailure {
  override def getMessage: String =
    s"""
       |Invalid values for genres entity:
       |following values are invalid:
       |${invalidValues.mkString}
       |They should be lesser than or equal to 50 characters.
       |""".stripMargin
}

case object RankingValidationFailure extends ValidationFailure {
  override def getMessage: String =
    "Invalid ranking value: should be between 0 and 10"
}
