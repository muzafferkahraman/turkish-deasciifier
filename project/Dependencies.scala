import sbt._

object Dependencies {
  val circeVersion = "0.12.3"
  val catsVersion = "1.3.0"

  val circeDependencies = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)
  val catsDependencies = Seq(
    "org.typelevel" %% "cats-effect"
  ).map(_ % catsVersion)
}
