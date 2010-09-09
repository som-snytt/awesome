import sbt._

class AwesomeProject(info: ProjectInfo) extends DefaultProject(info) {
  val localMaven   = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy     = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  
  val scalap    = "org.scala-lang" % "scalap" % "2.8.0" withSources()
  val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.2.1"
  val asm       = "asm" % "asm-debug-all" % "3.2"
  val specs     = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources()
}
