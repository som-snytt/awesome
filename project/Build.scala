import sbt._
import Keys._

object Awesome extends Build {
  lazy val root = Project("awesome", file("."), settings = Defaults.defaultSettings ++ awesomeSettings)
  
  def awesomeSettings = Seq(
    name := "awesome",
    organization := "org.improving",
    version := "0.5.3-SNAPSHOT",
    scalaVersion := "2.8.1",
    scalacOptions += "-deprecation",
    mainClass in (Compile, run) := Some("awesome.Main"),
    resolvers += ("Ivy" at "file://"+Path.userHome+"/.ivy2/local"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scalap" % "2.8.1",
      "org.scala-tools.testing" %% "specs" % "1.6.8" % "test",
      "com.codahale" % "assembly-sbt" % "0.1.1",
      "net.databinder" % "sxr-publish" % "0.2.0"
    )
  )
  def localSettings = Seq(
    scalaHome := Some(file("/scala/inst/3"))
  )
}