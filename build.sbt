
inThisBuild(
  List(
    name := "spark-smile",
    organization := "com.github.pierrenodet",
    homepage := Some(url(s"https://github.com/pierrenodet/spark-smile")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "pierrenodet",
        "Pierre Nodet",
        "nodet.pierre@gmail.com",
        url("https://github.com/pierrenodet"))
    ),
    crossScalaVersions := List("2.12.8", "2.11.12")
  )
)

libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.3" % Provided
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.3" % Provided
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.4.3" % Provided

libraryDependencies += "com.github.haifengl" %% "smile-scala" % "1.5.3"
libraryDependencies += "com.github.haifengl" % "smile-netlib" % "1.5.3"
libraryDependencies += "com.github.haifengl" % "smile-core" % "1.5.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test
libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" % Test

libraryDependencies += "com.holdenkarau" %% "spark-testing-base" % "2.4.3_0.12.0" % Test
libraryDependencies += "org.apache.spark" %% "spark-hive" % "2.4.3" % Test

fork in Test := true
parallelExecution in Test := false
javaOptions ++= Seq("-Xms512M", "-Xmx2048M", "-XX:MaxPermSize=2048M", "-XX:+CMSClassUnloadingEnabled")