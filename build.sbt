name := "dibs"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  "com.typesafe.play" %% "play-slick" % "0.5.0.8",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "org.mindrot" % "jbcrypt" % "0.3m",
   "com.typesafe" %% "play-plugins-mailer" % "2.2.0"
)

play.Project.playScalaSettings