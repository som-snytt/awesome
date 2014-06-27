package awesome
package asm
package visitor

import org.objectweb.asm._
import java.lang.reflect
import jvm.{ Attribute => _, _ }

class EmptyMethod extends MethodVisitor(ASM5) with MethodVisitorInterface

trait MethodVisitorInterface  {
  def visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor
  def visitAnnotationDefault(): AnnotationVisitor
  def visitAttribute(attr: Attribute): Unit
  def visitCode(): Unit
  def visitEnd(): Unit
  def visitFieldInsn(opcode: Int, owner: String, name: String, desc: String): Unit
  def visitFrame(jtype: Int, nLocal: Int, local: Array[AnyRef], nStack: Int, stack: Array[AnyRef]): Unit
  def visitIincInsn(jvar: Int, increment: Int): Unit
  def visitInsn(opcode: Int): Unit
  def visitInsnAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor
  def visitIntInsn(opcode: Int, operand: Int): Unit
  // def visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, bsmArgs: AnyRef*): Unit
  def visitJumpInsn(opcode: Int, label: Label): Unit
  def visitLabel(label: Label): Unit
  def visitLdcInsn(cst: AnyRef): Unit
  def visitLineNumber(line: Int, start: Label): Unit
  def visitLocalVariable(name: String, desc: String, signature: String, start: Label, end: Label, index: Int): Unit
  def visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath, start: Array[Label], end: Array[Label], index: Array[Int], desc: String, visible: Boolean): AnnotationVisitor
  def visitLookupSwitchInsn(default: Label, keys: Array[Int], labels: Array[Label]): Unit
  def visitMaxs(maxStack: Int, maxLocals: Int): Unit
  def visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean): Unit
  def visitMultiANewArrayInsn(desc: String, dims: Int): Unit
  def visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor
  // def visitTableSwitchInsn(min: Int, max: Int, dflt: Label, labels: Label*): Unit
  def visitTryCatchBlock(start: Label, end: Label, handler: Label, jtype: String): Unit
  def visitTryCatchAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor
  def visitTypeInsn(opcode: Int, jtype: String): Unit
  def visitTypeAnnotation(typeRef: Int, typePath: TypePath, desc: String, visible: Boolean): AnnotationVisitor
  def visitVarInsn(opcode: Int, jvar: Int): Unit
}

// visitTableSwitchInsn(int min, int max, Label dflt, Label... labels)
