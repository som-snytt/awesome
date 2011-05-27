import sbt._

class AwesomeProject(info: ProjectInfo) extends DefaultProject(info) with assembly.AssemblyBuilder with sxr.Publish {
  val localMaven   = "Maven" at "file://"+Path.userHome+"/.m2/repository"
  val localIvy     = "Ivy" at "file://"+Path.userHome+"/.ivy2/local"
  
  override def mainClass = Some("awesome.Main")
  override def sxr_artifact = "org.scala-tools.sxr" %% "sxr" % "0.2.7"
  
  val scalap = "org.scala-lang" % "scalap" % "2.8.1" // withSources()
  val specs  = "org.scala-tools.testing" %% "specs" % "1.6.8" % "test"
  // val paranamer = "com.thoughtworks.paranamer" % "paranamer" % "2.3"
  // val asm       = "asm" % "asm-debug-all" % "3.3"

  // override def libraryDependencies = super.libraryDependencies ++ (
  //   info.buildScalaVersion match {
  //     case Some(v) if v startsWith "2.8"  => Set("org.scala-tools.testing" %% "specs" % "1.6.6" % "test") // withSources()
  //       case _                              => Set()
  //   }
  // )
}
