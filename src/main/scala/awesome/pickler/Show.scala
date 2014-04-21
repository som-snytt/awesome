package awesome
package pickler

import scala.reflect.internal.pickling.PickleBuffer
import scala.tools.nsc.util.ShowPickled
import scala.tools.nsc.io.File
import jvm.ClassFileParser
import awesome.scalap.ScalaSigFinder

object Show {
  def apply(name: String): Unit = {
    ScalaSigFinder nameToBytes name foreach apply
  }

  def apply(data: Array[Byte]): Unit = {
    val pickle = new PickleBuffer(data, 0, data.length)
    ShowPickled.printFile(pickle, Console.out)
  }

  def main(args: Array[String]): Unit = {
    args foreach { x =>
      println(x + ":\n")
      apply(x)
      // apply(File(x).bytes().toList.toArray)
    }
  }
}
