package repositories

import cats.effect.MonadCancelThrow
import cats.implicits.toFunctorOps
import doobie.implicits.*
import doobie.postgres.*
import doobie.postgres.implicits.*
import doobie.{Fragment, Read, Transactor, Write}
import types.{Country, Genres, Movie, Ranking}
import types.Country.{Country, given}
import types.Genres.{Genres, given}
import types.Ranking.{Ranking, given}

import java.time.LocalDate

trait MoviesRepository[F[_]] {

  /** Stores the given Movie in the database.
    *
    * @param movie
    *   The movie Movie to store in DB.
    * @return
    *   Nothing if the Movie is stored, raises an error otherwise.
    */
  def create(movie: Movie): F[Unit]

  /** List all Movies present in the DB.
    *
    * @param genre
    *   Optional filter on genre.
    * @return
    *   All the movies stored in the DB satisfying the given criteria, raises an
    *   error otherwise.
    */
  def list(genre: Option[String] = None): F[List[Movie]]

  /** Counts all Movies stored in DB.
    *
    * @param year
    *   Optional filter on year of production.
    * @return
    *   The count of all the movies stored in DB satisfying the given criteria,
    *   raises an error otherwise.
    */
  def count(year: Option[Int] = None): F[Int]
}

object MoviesRepository {
  def apply[F[_]: MonadCancelThrow](xa: Transactor[F]): MoviesRepository[F] =
    new MoviesRepository[F] {

      private val insertFr: Fragment =
        fr"INSERT INTO movies (title, country, year, original_title, french_release, synopsis, genres, ranking)"

      private val selectFr: Fragment =
        fr"SELECT title, country, year, original_title, french_release, synopsis, genres, ranking FROM movies"

      private val countFr: Fragment = fr"SELECT COUNT(*) FROM movies"

      override def create(movie: Movie): F[Unit] = {
        val values: Fragment =
          fr""" VALUES (
               ${movie.title.value},
               ${movie.country.value},
               ${movie.year},
               ${movie.originalTitle},
               ${movie.frenchRelease},
               ${movie.synopsis},
               ${movie.genres.value},
               ${movie.ranking.value}
           )"""
        (insertFr ++ values).update
          .withUniqueGeneratedKeys[Int]("id")
          .transact(xa)
          .void
      }

      override def list(genre: Option[String] = None): F[List[Movie]] = {
        val where =
          genre.fold(Fragment.empty)(ge => fr" WHERE $ge = ANY(genres)")
        (selectFr ++ where ++ sql" ORDER BY year, title")
          .query[Movie]
          .to[List]
          .transact(xa)
      }

      override def count(year: Option[Int] = None): F[Int] = {
        val where = year.fold(Fragment.empty)(y => fr" WHERE year = $y")
        (countFr ++ where).query[Int].unique.transact(xa)
      }
    }
}
