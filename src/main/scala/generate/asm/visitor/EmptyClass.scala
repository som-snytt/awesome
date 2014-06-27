package awesome
package asm
package visitor

import org.objectweb.asm._
import java.lang.reflect
import jvm.{ Attribute => _, _ }
import Model._

class PartialClass[T](pf: Any =>? T) extends ClassVisitor(ASM5) {
  private var buf = new ListBuffer[T]
  private var isComplete = false
  private lazy val bufResult = buf.toList

  def record[R >: Null](x: Any): R = {
    if (pf isDefinedAt x)
      buf += pf(x)

    null
  }

  override def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]): Unit =
    record(Class(access, name, signature, superName, arrayToList(interfaces)))

  override def visitSource(source: String, debug: String) { }
  override def visitOuterClass(owner: String, name: String, desc: String) { }
  override def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor = null
  override def visitAttribute(attr: Attribute) { }

  override def visitInnerClass(name: String, outerName: String, innerName: String, access: Int): Unit =
    record(Class(access, name, null, null, null))

  override def visitField(access: Int, name: String, desc: String, signature: String, value: Object): FieldVisitor =
    record[FieldVisitor](Field(access, name, desc, signature, value))

  override def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor =
    record[MethodVisitor](Method(None, access, name, desc, signature, arrayToList(exceptions)))

  override def visitEnd() {
    synchronized {
      bufResult
      isComplete = true
    }
  }
  def apply(): List[T] = if (isComplete) bufResult else error("Not ready")
}

class EmptyVisitor extends ClassVisitor(ASM5) with ClassVisitorInterface

trait ClassVisitorInterface {
  def visit(version: Int, access: Int, name: String, signature: String, superName: String, interfaces: Array[String]): Unit
  def visitSource(source: String, debug: String): Unit
  def visitOuterClass(owner: String, name: String, desc: String): Unit
  def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor
  def visitAttribute(attr: Attribute): Unit
  def visitInnerClass(name: String, outerName: String, innerName: String, access: Int): Unit
  def visitField(access: Int, name: String, desc: String, signature: String, value: Object): FieldVisitor
  def visitMethod(access: Int, name: String, desc: String, signature: String, exceptions: Array[String]): MethodVisitor
  def visitEnd(): Unit
}
