package modules

import cats.Id
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import errors.ValidationFailure
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import repositories.MoviesRepository
import types.*

import java.time.LocalDate

class MoviesModuleTest extends AnyFlatSpec, Matchers {

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  private val fakeRepository: MoviesRepository[IO] = new MoviesRepository[IO] {
    def create(movie: Movie): IO[Unit] = IO.unit
    def list(genre: Option[String] = None): IO[List[Movie]] = IO.pure(Nil)
    def count(year: Option[Int] = None): IO[Int] = IO.pure(0)
  }

  private val moviesModule = MoviesModule[IO](fakeRepository)

  "MoviesModule" should "raise an error if a foreign movie has no original title" in {
    val validForeignMovie = Movie(
      title = Title("2001, l'Odyssée de l'espace"),
      country = Country("USA"),
      year = 1968,
      originalTitle = Some("2001 a space odyssey"),
      frenchRelease = Some(LocalDate.parse("1968-09-27")),
      synopsis = None,
      genres = Genres("SF" :: Nil),
      ranking = Ranking(8)
    )

    val invalidForeignMovie = Movie(
      title = Title("2001, l'Odyssée de l'espace"),
      country = Country("USA"),
      year = 1968,
      originalTitle = None,
      frenchRelease = Some(LocalDate.parse("1968-09-27")),
      synopsis = None,
      genres = Genres("SF" :: Nil),
      ranking = Ranking(8)
    )

    moviesModule
      .createMovie(validForeignMovie)
      .attempt
      .unsafeRunSync()
      .isRight shouldBe true

    assertThrows[ValidationFailure](
      moviesModule
        .createMovie(invalidForeignMovie)
        .unsafeRunSync()
    )
  }

}
