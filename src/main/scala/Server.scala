import cats.effect.{Async, ExitCode, IO, IOApp, Resource}
import com.comcast.ip4s.{ipv4, port}
import config.PostgresConfig
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import modules.MoviesModule
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import repositories.MoviesRepository
import routes.MoviesRoutes

object Server extends IOApp {
  private def app[F[_]: Async: Logger]: Resource[F, Server] =
    for {
      ec <- ExecutionContexts.fixedThreadPool[F](32)
      pgConfig <- PostgresConfig.load[F]
      xa <- HikariTransactor.newHikariTransactor(
        driverClassName = pgConfig.getDriver,
        url = pgConfig.getUrl,
        user = pgConfig.username,
        pass = pgConfig.password,
        connectEC = ec
      )
      repo = MoviesRepository[F](xa)
      module = MoviesModule[F](repo)
      routes = MoviesRoutes[F](module)
      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(routes.orNotFound)
        .build
      _ <- Resource.eval(
        Logger[F].info(
          s"server started and listening on port ${server.address.port}"
        )
      )
    } yield server

  override def run(args: List[String]): IO[ExitCode] = {
    given Logger[IO] = Slf4jLogger.getLogger[IO]
    app[IO].use(_ => IO.never.as(ExitCode.Success))
  }
}
