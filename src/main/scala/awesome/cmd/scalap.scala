package awesome
package cmd

import scala.tools.nsc.io._
import awesome.scalap.ByteCode
import awesome.jvm.ClassFileParser

object scalap {
  
  def usageMsg = """
    |usage: scalap {<option>} <name>
    |  -private           print private definitions
    |  -verbose           print out additional information
    |  -version           print out the version number of scalap
    |  -help              display this usage message
    |  -classpath <path>  specify where to find user class files
    |  -cp <path>         specify where to find user class files
  """.trim.stripMargin
    
  def usage = Console println usageMsg
  
  def main(args: Array[String]): Unit = {
    if (args.isEmpty)
      return usage
      
    val (paths, others) = args.toList partition (x => Path(x).exists)
    val targets: List[(String, List[Byte])] =
      (paths map (x => (x, File(x).bytes().toList))) :::
      (others map (x => (x, ByteCode(x).toList)))

    val classFiles = targets map (x => ClassFileParser(x._2).toOption)
    classFiles.flatten map (_.process) foreach println
  }
}
