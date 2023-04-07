package repositories

import cats.implicits.*
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import com.dimafeng.testcontainers.{ForEachTestContainer, PostgreSQLContainer}
import doobie.hikari.HikariTransactor
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import types.*

import java.time.LocalDate

class MoviesRepositoryTest
    extends AnyFlatSpec,
      Matchers,
      ForEachTestContainer,
      BeforeAndAfterAll {

  override val container: PostgreSQLContainer =
    new PostgreSQLContainer().configure { c =>
      c.withUsername("user")
      c.withPassword("password")
      c.withDatabaseName("postgres")
      c.withInitScript("create_tables.sql")
    }

  private def transactor = HikariTransactor.newHikariTransactor[IO](
    "org.postgresql.Driver",
    container.jdbcUrl,
    container.username,
    container.password,
    global.compute
  )

  private val movie = Movie(
    title = Title("2001, l'Odyssée de l'espace"),
    country = Country("USA"),
    year = 1968,
    originalTitle = Some("2001 a space odyssey"),
    frenchRelease = Some(LocalDate.parse("1968-09-27")),
    synopsis = None,
    genres = Genres("sf" :: Nil),
    ranking = Ranking(8)
  )

  override def beforeAll(): Unit = container.start()

  override def afterAll(): Unit = container.stop()

  "MoviesRepository" should "store Movie entities in DB" in {
    transactor
      .use { xa =>
        val moviesRepository = MoviesRepository[IO](xa)

        for {
          beforeCreate <- moviesRepository.count()
          _ <- moviesRepository.create(movie)
          afterCreate <- moviesRepository.count()
        } yield afterCreate shouldBe beforeCreate + 1
      }
      .unsafeRunSync()
  }

  it should "return all movies for a given genre" in {
    transactor
      .use { xa =>
        val moviesRepository = MoviesRepository[IO](xa)

        val comedy = Genres("comedy" :: Nil)
        val sf = Genres("sf" :: Nil)

        for {
          sfBefore <- moviesRepository.list(sf.value.headOption)
          _ <- moviesRepository.create(movie.copy(genres = comedy))
          sfAfterComedy <- moviesRepository.list(sf.value.headOption)
          // values are the the same: movie of another genre has been created
          _ <- IO.pure(sfBefore shouldBe sfAfterComedy)
          _ <- moviesRepository.create(
            movie.copy(Title("2002 a space odyssey"))
          )
          sfAfterSf <- moviesRepository.list(sf.value.headOption)
          // counts are different: we created another movie of the same genre
          _ <- IO.pure(sfAfterSf.length shouldBe sfBefore.length + 1)
          _ <- IO.pure(sfBefore.forall(sfAfterSf.contains) shouldBe true)
        } yield ()
      }
      .unsafeRunSync()
  }

  it should "return all movies if no genre is given" in {
    transactor
      .use { xa =>
        val moviesRepository = MoviesRepository[IO](xa)

        val comedy = Genres("comedy" :: Nil)
        val sf = Genres("sf" :: Nil)

        for {
          before <- moviesRepository.list()
          _ <- moviesRepository.create(movie.copy(genres = comedy))
          afterOne <- moviesRepository.list()
          _ <- IO.pure(afterOne.length shouldBe before.length + 1)
          _ <- IO.pure(before.forall(afterOne.contains) shouldBe true)
          _ <- moviesRepository.create(
            movie.copy(Title("2002 a space odyssey"))
          )
          afterTwo <- moviesRepository.list()
          _ <- IO.pure(afterTwo.length shouldBe afterOne.length + 1)
          _ <- IO.pure(afterOne.forall(afterTwo.contains) shouldBe true)
        } yield ()
      }
      .unsafeRunSync()
  }

  it should "count all movies for a given year" in {
    transactor
      .use { xa =>
        val moviesRepository = MoviesRepository[IO](xa)

        for {
          _ <- moviesRepository.create(movie)
          count <- moviesRepository.count(movie.year.some)
          _ <- moviesRepository.create(movie.copy(year = 2001))
          countAfterDiff <- moviesRepository.count(movie.year.some)
          // counts are same beacause year are different
          _ <- IO.pure(countAfterDiff shouldBe count)
          _ <- moviesRepository.create(
            movie.copy(title = Title("2002 a space odyssey"))
          )
          countAfterSame <- moviesRepository.count(movie.year.some)
          _ <- IO.pure(countAfterSame shouldBe count + 1)
        } yield ()
      }
      .unsafeRunSync()
  }

  it should "count all movies" in {
    transactor
      .use { xa =>
        val moviesRepository = MoviesRepository[IO](xa)

        for {
          _ <- moviesRepository.create(movie)
          countOne <- moviesRepository.count()
          _ <- moviesRepository.create(movie.copy(year = 2001))
          countTwo <- moviesRepository.count()
          _ <- IO.pure(countTwo shouldBe countOne + 1)
          _ <- moviesRepository.create(
            movie.copy(title = Title("2002 a space odyssey"))
          )
          countThree <- moviesRepository.count()
          _ <- IO.pure(countThree shouldBe countTwo + 1)
        } yield ()
      }
      .unsafeRunSync()
  }

}
