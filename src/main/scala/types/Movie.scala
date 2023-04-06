package types

import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import types.Country.Country
import types.Genres.Genres
import types.Ranking.Ranking
import types.Title.Title

import java.time.LocalDate

given Configuration = Configuration.default.withSnakeCaseMemberNames

object Movie {
  final val MAX_TITLE_LENGTH = 150
}

case class Movie(
    title: Title,
    country: Country,
    year: Int,
    originalTitle: Option[String],
    frenchRelease: Option[LocalDate],
    synopsis: Option[String],
    genres: Genres,
    ranking: Ranking
) derives ConfiguredCodec
