name := "dibs"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.typesafe.play" %% "play-slick" % "0.5.0.8",
  "org.mindrot" % "jbcrypt" % "0.3m",
  "postgresql" % "postgresql" % "8.4-702.jdbc4"
)

play.Project.playScalaSettings