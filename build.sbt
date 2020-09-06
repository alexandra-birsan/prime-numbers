name := "prime-numbers"

version := "0.1"

scalaVersion := "2.12.10"

lazy val finagleVersion = "20.8.1"
lazy val finchVersion = "0.31.0"

libraryDependencies ++= Seq(
  "com.twitter" %% "finagle-http" % finagleVersion,
  "com.twitter" %% "finagle-thriftmux"  % finagleVersion,
  "com.twitter" %% "finagle-thrift"  % finagleVersion,
  "com.twitter" %% "scrooge-core" % finagleVersion,
  "com.twitter" %% "twitter-server" % finagleVersion,
  "com.github.finagle" %% "finch-core" % finchVersion,
  "com.github.finagle" %% "finch-circe" % finchVersion,

  "io.circe" %% "circe-generic" % "0.12.2",
  "com.github.finagle" %% "finch-test" % finchVersion % Test,
)


libraryDependencies := libraryDependencies.value.map(_ .exclude("org.slf4j", "slf4j-jdk14")
  .exclude("org.slf4j", "slf4j-log4j12"))
