lazy val root = project in file(".") settings (
                         name := "awesome",
                 organization := "org.improving",
                      version := "0.5.5-SNAPSHOT",
                 scalaVersion := "2.11.0",
  mainClass in (Compile, run) := Some("awesome.Main"),
   initialCommands in console := "import awesome._",
          libraryDependencies ++= Seq(
            "org.scala-lang.modules" %% "scala-parser-combinators" %      "1.0.1",
            "org.scala-lang"          % "scala-reflect"            % scalaVersion.value,
            "org.scala-lang"          % "scalap"                   % scalaVersion.value
          )
)
