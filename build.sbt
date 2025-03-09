lazy val version = "1.0.0"

ThisBuild / scalaVersion := "3.3.3"

Global / excludeLintKeys += idePackagePrefix

resolvers ++= Seq(
  "IntelliJ" at "https://www.jetbrains.com/intellij-repository/releases",
  "IntelliJ Deps" at "https://cache-redirector.jetbrains.com/intellij-dependencies")

lazy val root = (project in file("."))
  .settings(
    name := "SearcherSwing",
    idePackagePrefix := Some("org.satyagraha.searcher"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.5.16",
      "co.fs2" %% "fs2-core" % "3.11.0",
      "co.fs2" %% "fs2-io" % "3.11.0",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.7",
      "com.google.guava" % "guava" % "23.0",
      "com.jetbrains.intellij.java" % "java-gui-forms-rt" % "241.18034.82",
      "com.typesafe" % "config" % "1.4.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.scala-lang.modules" %% "scala-swing" % "3.0.0",
      "org.typelevel" %% "mouse" % "1.3.2"
    ),
    assembly / assemblyJarName := s"Searcher-${version}.jar",
    assembly / assemblyMergeStrategy := { // q.v. https://stackoverflow.com/a/74212770
      case PathList("META-INF", xs@_*) =>
        xs.map(_.toLowerCase) match {
          case "services" :: xs => MergeStrategy.filterDistinctLines
          case _ => MergeStrategy.discard
        }
      case _ => MergeStrategy.preferProject
    },
    assembly / mainClass := Some("org.satyagraha.searcher.app.Searcher"),
  )

scalacOptions ++= Seq(
  "-deprecation",
  "-Xfatal-warnings"
)
