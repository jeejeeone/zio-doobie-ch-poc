ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "zio-doobie",
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % "2.0.13",
      "com.clickhouse" % "clickhouse-jdbc" % "0.4.6",
      "com.clickhouse" % "clickhouse-http-client" % "0.4.6",
      "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
      "org.tpolecat" %% "doobie-postgres" % "1.0.0-RC2",
      "io.github.gaelrenoux" %% "tranzactio" % "4.1.0",
      "org.lz4" % "lz4-java" % "1.8.0",
      "org.playframework.anorm" %% "anorm" % "2.7.0",
      "io.github.gaelrenoux" %% "tranzactio-core" % "5.0.1",
      "io.github.gaelrenoux" %% "tranzactio-anorm" % "5.0.1",
      "dev.zio" %% "zio-test" % "2.0.13" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
