import sbt._

class Plugins(info: sbt.ProjectInfo) extends sbt.PluginDefinition(info) {
  val codaRepo = "Coda Hale's Repository" at "http://repo.codahale.com/"
  val assemblySBT = "com.codahale" % "assembly-sbt" % "0.1.1"
  val sxr_publish = "net.databinder" % "sxr-publish" % "0.2.0"
}
