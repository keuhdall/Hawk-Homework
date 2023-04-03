package modules

import cats.MonadThrow
import cats.effect.kernel.Resource
import io.circe.*
import io.circe.derivation.Configuration
import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import types.Country.Country
import types.Genres.Genres
import types.Ranking.Ranking

import java.time.LocalDate

case class Movie(
    title: String, // TODO: max 250 char
    country: Country,
    year: Int,
    originalTitle: Option[String],
    frenchRelease: Option[LocalDate],
    synopsis: Option[String],
    genres: Genres,
    ranking: Ranking
)

object Movie {
  given Configuration = Configuration.default.withSnakeCaseMemberNames
  given Decoder[Movie] = deriveDecoder[Movie]
  given Encoder[Movie] = deriveEncoder[Movie]
}

trait MoviesModule[F[_]] {

  /** Checks the given movie entity and stores in DB if valid.
    *
    * @param movie
    *   The movie entity to store.
    * @return
    *   Nothing if the entity is stored, raises an error otherwise.
    */
  def createMovie(movie: Movie): F[Unit]

  /** Returns all the movies, we can optionally filter by genre.
    *
    * @param genre
    *   The genre of the movie we want to filter on.
    * @return
    *   All the movies satisfying the given criteria, raises an error otherwise.
    */
  def getMovies(genre: Option[String]): F[List[Movie]]

  /** Returns the count of all movies stored in the database, we can optionally
    * filter by year of production.
    *
    * @param year
    *   The year of production of the movie we want to filter on.
    * @return
    *   The count of movies satisfying the given criteria, raises an error
    *   otherwise.
    */
  def getMoviesCount(year: Option[Int]): F[Int]
}

object MoviesModule {
  def apply[F[_]: MonadThrow]: Resource[F, MoviesModule[F]] =
    Resource.pure(new MoviesModule[F] { // TODO: Change how resource is acquired
      override def createMovie(movie: Movie): F[Unit] = MonadThrow[F].unit

      override def getMovies(genre: Option[String]): F[List[Movie]] =
        MonadThrow[F].pure(Nil)

      override def getMoviesCount(year: Option[Int]): F[Int] =
        MonadThrow[F].pure(0)
    })
}
