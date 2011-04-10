package awesome
package jvm

import attr.{ Code, Exceptions, LocalVariableTable, LocalVariableTypeTable, LocalVariableTableBase }
import java.lang.reflect

case class MethodInfo(
  owner: ClassInfo,
  access: Flags,
  id: Ident,
  descriptor: MethodDescriptor,
  attributes: List[Attribute]
) extends FieldOrMethod {
  
  type SigType = MethodSignature
  def createSignature = x => MethodSignature(x, name.toScalaString)
  def thisMethodSymbol = owner.methodSymbolFor(id)
  def symbolTypeParams =
    if (thisMethodSymbol.isEmpty) Nil
    else ScalapView.typeSymbols(thisMethodSymbol.get.children)
  
  def isImplicit = thisMethodSymbol exists (_.isImplicit)
  
  def isVarArgs = access.isVarArgs
  def varArgType = jmethod flatMap (_.varArgType)
    
  def isSetter = name.isSetter
  def isConstructor = name.isConstructor
  def isExpandedName = name.isExpanded
  def isJavaMain = name == "main" && descriptor.text == "([Ljava/lang/String;)V"  
  
  def shouldPrint = (
    !name.isSerializingMethod && !access.isBridge && !isSynthetic && 
    !name.containsDollarSign && !isSetter && !isExpandedName && !name.isStaticConstructor
  )

  private lazy val jmethod = owner reflectMethodFor this map (x => new JavaMethod(x))
  
  lazy val reflectMethod        = jmethod map (_.method)
  lazy val reflectTypeParams    = jmethod map (_.typeParams)
  lazy val reflectArgumentTypes = jmethod map (_.argumentTypes)
  lazy val reflectReturnType    = jmethod map (_.returnType)
  lazy val reflectMethodName    = jmethod map (_.methodName)
  
  def code          = findAttr({ case x: Code => x })
  def exceptions    = attributes flatMap { case Exceptions(xs) => xs ; case _ => Nil }
  
  def lineNumberTable         = code flatMap (_.lineNumberTable)
  def localVariableTable      = code flatMap (_.localVariableTable)
  def localVariableTypeTable  = code flatMap (_.localVariableTypeTable)
  def lookInTables[T](f: LocalVariableTableBase[_, _] => Option[T]): Option[T] =
    (localVariableTypeTable flatMap f) orElse (localVariableTable flatMap f)

  def nameAtIndex(x: Int)     = lookInTables[String](_.names get x)
  def typeAtIndex(x: Int)     = lookInTables[String](_.typesText get x)

  private def paramIndices    = {
    val xs = descriptor.argTypes.indices.toList
    if (access.isStatic) xs else xs map (_ + 1)
  }
  def paramNames  = paramIndices map nameAtIndex flatten
  // XXX this works, but we temporarily use reflection until our Type model is equally expressive
  // def paramTypes: List[String]  = {
  //   val xs = (paramIndices map typeAtIndex).flatten
  //   if (xs.size == paramIndices.size) xs
  //   else descriptor.argTypes map (_.toString)
  // }
  def paramTypes: List[String] = {
    val xs = reflectArgumentTypes
    if (xs.isEmpty)               descriptor.argTypes map (_.toString)
    else if (varArgType.isEmpty)  xs.get map javaTypeToString
    else                          xs.get.init :+ varArgType.get map javaTypeToString
  }
  private val skipPrinting = List("Code", "Deprecated")
  override def toString = ScalaFormatter.signature(this)
}
