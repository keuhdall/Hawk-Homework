package types

import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import types.Country.Country
import types.Genres.Genres
import types.Ranking.Ranking

import java.time.LocalDate

given Configuration = Configuration.default.withSnakeCaseMemberNames

case class Movie(
    title: String, // TODO: max 250 char
    country: Country,
    year: Int,
    originalTitle: Option[String],
    frenchRelease: Option[LocalDate],
    synopsis: Option[String],
    genres: Genres,
    ranking: Ranking
) derives ConfiguredCodec
