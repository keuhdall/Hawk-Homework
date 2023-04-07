package routes

import cats.{ApplicativeError, MonadThrow}
import cats.effect.Async
import cats.implicits.{
  catsSyntaxApply,
  catsSyntaxApplicativeError,
  toFunctorOps,
  toFlatMapOps
}
import errors.ValidationFailure
import io.circe.Json
import modules.{MoviesModule, given}
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.{HttpRoutes, InvalidMessageBodyFailure, Response}
import org.typelevel.log4cats.Logger
import types.Movie

object Genre extends OptionalQueryParamDecoderMatcher[String]("genre")
object Year extends OptionalQueryParamDecoderMatcher[Int]("year")

extension (msg: String) {
  def toJsonError: Json = Json.obj("error" -> Json.fromString(msg))
}

object MoviesRoutes {
  def apply[F[_]: Async: Logger](
      moviesModule: MoviesModule[F]
  ): HttpRoutes[F] = {
    object dsl extends Http4sDsl[F]; import dsl.*

    def handleFacingErrors[E <: Throwable](e: E): F[Response[F]] = e match {
      case vf: ValidationFailure =>
        BadRequest(vf.toJson)
      case bodyFailure: InvalidMessageBodyFailure =>
        BadRequest(
          bodyFailure.cause
            .fold(bodyFailure.getMessage)(_.getMessage)
            .toJsonError
        )
      case _ =>
        Logger[F].error(e)(
          s"an unexpected error occurred when processing user query: ${e.getMessage}"
        ) *> InternalServerError("an unexpected error occurred".toJsonError)
    }

    HttpRoutes.of[F] {
      case req @ POST -> Root / "movie" =>
        (for {
          _ <- Logger[F].info(
            s"received POST on /movie from ${req.from.getOrElse("<unknown>")}"
          )
          movie <- req.as[Movie]
          _ <- moviesModule.createMovie(movie)
          response <- Ok()
        } yield response)
          .handleErrorWith(handleFacingErrors)
      case req @ GET -> Root / "movies" :? Genre(genre) =>
        Logger[F].info(
          s"received GET on /movies from ${req.from.getOrElse("<unknown>")}"
        ) *> Ok(moviesModule.getMovies(genre))
          .handleErrorWith(handleFacingErrors)
      case req @ GET -> Root / "count" :? Year(year) =>
        Logger[F].info(
          s"received GET on /count from ${req.from.getOrElse("<unknown>")}"
        ) *> Ok(moviesModule.getMoviesCount(year))
          .handleErrorWith(handleFacingErrors)
    }
  }
}
