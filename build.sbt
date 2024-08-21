ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.19"

lazy val root = (project in file("."))
  .settings(
    name := "final",
    libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.144-R12"
  )

