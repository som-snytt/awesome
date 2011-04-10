package awesome.scalap

import java.lang.{ ClassLoader => JavaClassLoader }
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.scalap._
import Main.{ SCALA_SIG, SCALA_SIG_ANNOTATION, BYTES_VALUE }
import scalax.rules.{ scalasig => sp }
import sp.{ ClassFile, ClassFileParser, ScalaSig, ScalaSigParser, ScalaSigAttributeParsers, StringBytesPair }
import ClassFileParser.{ ConstValueIndex, Annotation }
import scala.reflect.generic.{ ByteCodecs, PickleBuffer }

object ScalaSigFinder {  
  def defaultLoaders = List[ScalaClassLoader](
    ScalaClassLoader.getSystemLoader,
    new JavaClassLoader(Thread.currentThread.getContextClassLoader) with ScalaClassLoader
  )
  def findClazz(name: String): List[Class[_]] = defaultLoaders map (_ tryToLoadClass name) flatten
  
  def nameToPickleBuffer(name: String) =
    nameToBytes(name) map (x => new PickleBuffer(x, 0, x.length))
  
  def nameToBytes(name: String): Option[Array[Byte]] =
    findClazz(name) flatMap bytes headOption
  
  def bytes(clazz: Class[_]): Option[Array[Byte]] = {
    import ScalaSigParser._
    val byteCode  = sp.ByteCode.forClass(clazz)
    val classFile = ClassFileParser.parse(byteCode)

    scalaSigBytesFromAnnotation(classFile)
  }
  def scalaSigBytesFromAnnotation(classFile: ClassFile): Option[Array[Byte]] = {
    import ScalaSigParser._
    import classFile._

    classFile.annotation(SCALA_SIG_ANNOTATION) map {
      case Annotation(_, elements) =>
        val bytesElem = elements.find(elem => constant(elem.elementNameIndex) == BYTES_VALUE).get
        val bytes = ((bytesElem.elementValue match {case ConstValueIndex(index) => constantWrapped(index)})
                .asInstanceOf[StringBytesPair].bytes)
        val length = ByteCodecs.decode(bytes)
      
        bytes take length
    }
  }  
  
  def fromClazz(clazz: Class[_]): Option[ScalaSig] =
    ScalaSigParser parse clazz
  
  def fromName(name: String): Option[ScalaSig] = {
    for (loader <- defaultLoaders) {
      val clazzOpt: Option[Class[_]] = loader tryToLoadClass name
      for (clazz <- clazzOpt ; sig <- fromClazz(clazz)) {
        return Some(sig)
      }
    }
    None
  }
  // 
  // def fromName(name: String): Option[ScalaSig] =
  //   defaultLoaders flatMap (_ tryToLoadClass name flatMap fromClazz toList) headOption
  
  def apply(name: String) = fromName(name)
}
