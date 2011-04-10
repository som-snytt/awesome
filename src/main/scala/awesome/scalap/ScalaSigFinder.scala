package awesome.scalap

import java.lang.{ ClassLoader => JavaClassLoader }
import scala.tools.nsc.util.ScalaClassLoader
import scala.tools.scalap._
import Main.{ SCALA_SIG, SCALA_SIG_ANNOTATION, BYTES_VALUE }
import scalax.rules.{ scalasig => sp }
import sp.{ ClassFile, ClassFileParser, ScalaSig, ScalaSigAttributeParsers, StringBytesPair }
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
  
  def bytes(clazz: Class[_]): Option[Array[Byte]] =
    CopiedScalaSigParser bytes clazz
  
  def fromClazz(clazz: Class[_]): Option[ScalaSig] =
    CopiedScalaSigParser parse clazz
  
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

  /** Basically copied from trunk since it missed 2.8.0. */
  object CopiedScalaSigParser {
    def bytes(clazz : Class[_]): Option[Array[Byte]] = {
      val byteCode  = sp.ByteCode.forClass(clazz)
      val classFile = ClassFileParser.parse(byteCode)

      scalaSigBytesFromAnnotation(classFile)
    }
    def scalaSigBytesFromAnnotation(classFile: ClassFile): Option[Array[Byte]] = {
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
    
    def scalaSigFromAnnotation(classFile: ClassFile): Option[ScalaSig] =
      scalaSigBytesFromAnnotation(classFile) map (xs => ScalaSigAttributeParsers.parse(sp.ByteCode(xs)))

    def scalaSigFromAttribute(classFile: ClassFile) : Option[ScalaSig] =
      classFile.attribute(SCALA_SIG).map(_.byteCode).map(ScalaSigAttributeParsers.parse)
    
    def parse(classFile: ClassFile): Option[ScalaSig] = {
      val scalaSig  = scalaSigFromAttribute(classFile)
    
      scalaSig match {
        // No entries in ScalaSig attribute implies that the signature is stored in the annotation
        case Some(ScalaSig(_, _, entries)) if entries.length == 0 =>
          scalaSigFromAnnotation(classFile)
        case x => x
      }
    }
    
    def parse(clazz : Class[_]): Option[ScalaSig] = {
      val byteCode  = sp.ByteCode.forClass(clazz)
      val classFile = ClassFileParser.parse(byteCode)

      parse(classFile)
    }
  }
}