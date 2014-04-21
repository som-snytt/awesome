lazy val root = project in file(".") settings (
                         name := "awesome",
                 organization := "org.improving",
                      version := "0.5.4-SNAPSHOT",
                 scalaVersion := "2.11.0",
  mainClass in (Compile, run) := Some("awesome.Main"),
   initialCommands in console := "import awesome._",
          libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
          libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
          libraryDependencies += "org.scala-lang" % "scalap" % scalaVersion.value
)
