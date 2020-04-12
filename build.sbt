import Dependencies._

ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "1.0.0"
ThisBuild / organization     := "me.ekahraman"
ThisBuild / name             := "turkish-deasciifier"

lazy val core = project in file("core")
lazy val app = (project in file("app"))
  .dependsOn(core)
  .settings(
    libraryDependencies ++=
        circeDependencies ++
        catsDependencies
  )

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:higherKinds",
  "-Ypartial-unification"
)


