package config

import cats.MonadThrow
import cats.effect.Resource
import cats.implicits.*
import errors.{ConfigError, PgConfigError}
import pureconfig.error.ConfigReaderFailures
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.derivation.default.*

final case class PostgresConfig(
    host: String,
    database: String,
    username: String,
    password: String,
    port: Int
) derives ConfigReader {
  def getDriver: String = "org.postgresql.Driver"
  def getUrl: String = s"jdbc:postgresql://$host/$database"
}

extension [A](res: ConfigReader.Result[A])
  def toResource[F[_]: MonadThrow](
      f: ConfigReaderFailures => ConfigError
  ): Resource[F, A] = Resource.Eval(MonadThrow[F].fromEither(res.leftMap(f)))

object PostgresConfig {
  def load[F[_]: MonadThrow]: Resource[F, PostgresConfig] =
    ConfigSource.default
      .at("postgres")
      .load[PostgresConfig]
      .toResource[F](PgConfigError.apply)
}
