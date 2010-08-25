import sbt._

class AwesomeProject(info: ProjectInfo) extends DefaultProject(info) {
  val localMaven   = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy     = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  
  val specs      = "org.scala-tools.testing" %% "specs" % "1.6.5" % "test" withSources()
}
