package modules

import cats.effect.MonadCancelThrow
import cats.implicits.{catsSyntaxApply, toFunctorOps, toFlatMapOps}
import errors.MissingOriginalTitleError
import org.typelevel.log4cats.Logger
import repositories.MoviesRepository
import types.{Movie, given}

import java.time.LocalDate

trait MoviesModule[F[_]] {

  /** Checks the given movie entity and stores in DB if valid.
    *
    * @param movie
    *   The movie entity to store.
    * @return
    *   Nothing if the entity is stored, raises an error otherwise.
    */
  def createMovie(movie: Movie): F[Unit]

  /** Get all the movies, we can optionally filter by genre.
    *
    * @param genre
    *   The genre of the movie we want to filter on.
    * @return
    *   All the movies satisfying the given criteria, raises an error otherwise.
    */
  def getMovies(genre: Option[String]): F[List[Movie]]

  /** Counts of all movies, we can optionally filter by year of production.
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
  def apply[F[_]: MonadCancelThrow: Logger](
      moviesRepo: MoviesRepository[F]
  ): MoviesModule[F] = new MoviesModule[F] {
    override def createMovie(movie: Movie): F[Unit] =
      MonadCancelThrow[F].raiseWhen(
        movie.country.value != "FRA" && movie.originalTitle.isEmpty
      )(MissingOriginalTitleError) *>
        moviesRepo.create(movie) *>
        Logger[F].debug(s"movie successfully created")

    override def getMovies(genre: Option[String]): F[List[Movie]] =
      for {
        movies <- moviesRepo.list(genre)
        _ <- Logger[F].debug(s"successfully retrieved ${movies.length} results")
      } yield movies

    override def getMoviesCount(year: Option[Int]): F[Int] =
      for {
        count <- moviesRepo.count(year)
        _ <- Logger[F].debug(s"successfully retrieved $count results")
      } yield count
  }
}
