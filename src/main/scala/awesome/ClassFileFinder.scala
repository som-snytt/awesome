package awesome

import jvm.ClassFile

object ClassFileFinder extends BytecodeFinder[List[ClassFile]]() {
  def trace(x: Class[_]): List[ClassFile] = apply(x.getName) getOrElse Nil
  def trace(x: AnyRef): List[ClassFile]   = trace(x.getClass)
  
  def creator(xs: Array[Byte]) = CF.fromBytes(xs).toList
}
