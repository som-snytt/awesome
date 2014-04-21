package awesome

import java.lang.{ ClassLoader => JavaClassLoader }
import scala.reflect.internal.util.ScalaClassLoader
import scala.reflect.io.File

/** Tries its hardest to find the bytes which make up a class.
 */
abstract class BytecodeFinder[+T](val loaders: List[ScalaClassLoader]) {
  // abstract method called if bytecode bytes are found
  def creator(xs: Array[Byte]): T

  def this() = this(BytecodeFinder.defaultLoaders)

  def bytecode(name: String) = loaders map (_ classBytes name) find (_.nonEmpty)

  def fromFile(fileName: String)  = {
    val f = File(fileName)
    if (f.exists) Some(creator(f.toByteArray))
    else None
  }
  def fromName(name: String) =
    bytecode(name) map creator

  def apply(name: String) =
    fromName(name) orElse fromFile(name)
}

object BytecodeFinder {
  def defaultLoaders = List[ScalaClassLoader](
    ScalaClassLoader(null),
    new JavaClassLoader(Thread.currentThread.getContextClassLoader) with ScalaClassLoader
  )
}
