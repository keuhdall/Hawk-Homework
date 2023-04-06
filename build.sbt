ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.2"

lazy val root = (project in file("."))
  .settings(
    name := "Hawk-Homework",
    dockerExposedPorts ++= Seq(5005, 8080),
    commonSettings,
    Universal / javaOptions ++= Seq("-jvm-debug 5005"),
    libraryDependencies ++=
      commonDeps ++
        circeDeps ++
        doobieDeps ++
        http4sDeps ++
        logDeps ++
        testDeps ++
        Seq(deps.pureConfig)
  )
  .enablePlugins(ScalafmtPlugin)
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-Yretain-trees",
    "-language:implicitConversions",
    "-feature"
  )
) ++ scalafmtSettings

lazy val scalafmtSettings =
  Seq(scalafmtOnCompile := true)

lazy val commonDeps = Seq(deps.cats, deps.catsEffect)
lazy val circeDeps =
  Seq(deps.circe, deps.circeGeneric, deps.circeParser)
lazy val doobieDeps = Seq(deps.doobie, deps.doobieHikari, deps.doobiePostgres)
lazy val http4sDeps =
  Seq(deps.http4sClient, deps.http4sServer, deps.http4sCirce, deps.http4sDsl)
lazy val logDeps = Seq(deps.log4cats, deps.slf4j)
lazy val testDeps =
  Seq(deps.scalaTest, deps.scalaCheck, deps.scalaTestPlusCheck)

lazy val deps = new {
  val catsVersion = "2.9.0"
  val catsEffectVersion = "3.4.8"
  val circeVersion = "0.14.5"
  val doobieVersion = "1.0.0-RC2"
  val http4sVersion = "1.0.0-M39"
  val log4catsVersion = "2.5.0"
  val pureConfigVersion = "0.17.2"
  val slf4jVersion = "2.0.5"
  val scalaTestVersion = "3.2.15"
  val scalaCheckVersion = "1.17.0"
  val scalaTestPlusCheckVersion = "3.2.15.0"

  val cats = "org.typelevel" %% "cats-core" % catsVersion
  val catsEffect = "org.typelevel" %% "cats-effect" % catsEffectVersion

  val circe = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion

  val doobie = "org.tpolecat" %% "doobie-core" % doobieVersion
  val doobieHikari = "org.tpolecat" %% "doobie-hikari" % doobieVersion
  val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % doobieVersion

  val http4sClient = "org.http4s" %% "http4s-ember-client" % http4sVersion
  val http4sServer = "org.http4s" %% "http4s-ember-server" % http4sVersion
  val http4sCirce = "org.http4s" %% "http4s-circe" % http4sVersion
  val http4sDsl = "org.http4s" %% "http4s-dsl" % http4sVersion

  val log4cats = "org.typelevel" %% "log4cats-slf4j" % log4catsVersion

  val pureConfig =
    "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion

  val slf4j = "org.slf4j" % "slf4j-simple" % slf4jVersion

  val scalaTest = "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % scalaCheckVersion % "test"
  val scalaTestPlusCheck =
    "org.scalatestplus" %% "scalacheck-1-17" % scalaTestPlusCheckVersion % "test"
}
