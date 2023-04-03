package routes

import cats.effect.{Async, Concurrent}
import cats.implicits.*
import io.circe.Json
import modules.{Movie, MoviesModule, given}
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.{EntityDecoder, HttpRoutes}

object Genre extends OptionalQueryParamDecoderMatcher[String]("genre")
object Year extends OptionalQueryParamDecoderMatcher[Int]("year")

object MoviesRoutes {
  def apply[F[_]: Async](moviesModule: MoviesModule[F]): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl.*

    HttpRoutes.of[F] {
      case req @ POST -> Root / "movie" =>
        Ok(req.as[Movie] >>= moviesModule.createMovie)
      case GET -> Root / "movies" :? Genre(genre) =>
        Ok(moviesModule.getMovies(genre))
      case GET -> Root / "count" :? Year(year) =>
        Ok(moviesModule.getMoviesCount(year))
    }
  }
}
