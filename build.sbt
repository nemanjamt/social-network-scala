name := """social-network"""
organization := "novalite"
version := "1.0-SNAPSHOT"
lazy val root = (project in file(".")).enablePlugins(PlayScala)
scalaVersion := "2.13.12"
libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.0" % Test
libraryDependencies ++= Seq(
  "org.playframework" %% "play-slick" % "6.0.0",
  "org.playframework" %% "play-slick-evolutions" % "6.0.0",
  "com.mysql" % "mysql-connector-j" % "8.0.33"
)
libraryDependencies += "com.github.jwt-scala" %% "jwt-play-json" % "10.0.0"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
