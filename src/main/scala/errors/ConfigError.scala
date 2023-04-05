package errors

import pureconfig.error.ConfigReaderFailures

sealed trait ConfigError extends Throwable

case class PgConfigError(failures: ConfigReaderFailures) extends ConfigError {
  override def getMessage: String =
    s"Failed to load postgres config: ${failures.prettyPrint()}"
}
