import cats.effect.kernel.{Async, Resource}
import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{ipv4, port}
import modules.MoviesModule
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import routes.MoviesRoutes

object Server extends IOApp {
  private def app[F[_]: Async]: Resource[F, Server] =
    for {
      module <- MoviesModule[F]
      routes = MoviesRoutes(module)
      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(routes.orNotFound)
        .build
    } yield server

  override def run(args: List[String]): IO[ExitCode] =
    app[IO].use(_ => IO.never.as(ExitCode.Success))
}
