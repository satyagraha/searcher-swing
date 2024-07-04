ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "SearcherSwing",
    idePackagePrefix := Some("org.satyagraha.searcher"),
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.10.2",
      "co.fs2" %% "fs2-io" % "3.10.2",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "com.typesafe" % "config" % "1.4.3",
      "org.typelevel" %% "mouse" % "1.3.0"
    )
  )
