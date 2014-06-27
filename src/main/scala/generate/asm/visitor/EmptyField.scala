package awesome
package asm
package visitor

import org.objectweb.asm._

class EmptyField extends FieldVisitor(ASM5) with FieldVisitorInteface

trait FieldVisitorInteface {
  def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor
  def visitAttribute(attr: Attribute): Unit
  def visitEnd(): Unit
}
