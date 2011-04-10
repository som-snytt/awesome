package awesome
package jvm

import Flags._

trait Formatter {
  def accessFlags(f: Flags): String
  def otherFlags(f: Flags): String
  def flagName(code: Int): String
  def methodFlagName(code: Int): String

  def methodName(m: MethodInfo): String
  def tparams(m: MethodInfo): String
  def params(m: MethodInfo): String
  def returnType(m: MethodInfo): String
  def throws(m: MethodInfo): String
  
  def ident(id: Ident): String
  
  def signature(m: MethodInfo): String 
}

object ScalaFormatter extends Formatter {
  def accessFlags(f: Flags) = {
    if (f.isPublic) ""
    else if (f.isProtected) "protected"
    else if (f.isPrivate) "private"
    else if (f.pkg == "") "private[<package>]"
    else "private[%s]".format(f.pkg)
  }
  def otherFlags(f: Flags) = {
    val words =
      if (f.isMethod) f.setFlags map methodFlagName
      else f.setFlags map flagName
    
    spaceSepString(words: _*)
  }
  def scalaFlags(m: MethodInfo) = m.thisMethodSymbol match {
    case Some(s)  => if (s.isImplicit) "implicit" else ""
    case _        => ""
  }
  
  def ident(id: Ident): String = Ident.asScala(id.name, id.typeArgs)
  def methodFlagName(code: Int) = code match {
    case ACC_SYNCHRONIZED => "synchronized"
    case _                => flagName(code)
  }  
  def flagName(code: Int) = code match {
    case ACC_FINAL        => "final"
    case ACC_VOLATILE     => "@volatile"
    case ACC_TRANSIENT    => "@transient"
    case ACC_NATIVE       => "@native"
    case _                => ""
  }

  def methodName(m: MethodInfo) = 
    if (m.name.isConstructor) "this"
    else if (m.name != "%s") m.name
    else m.reflectMethodName getOrElse "<unknown>"

  def methodSig(m: MethodInfo) = {
    m.thisMethodSymbol map (x => ScalapView.tpString(x.infoType)) getOrElse ""
  }

  def tparams(m: MethodInfo): String = {
    m.thisMethodSymbol map (x => ScalapView.typeParamsString(x)) getOrElse ""
    // tparamsToString(m.symbolTypeParams map (x => ScalapView.typeParam(x)))
    // tparamsToString(m.reflectTypeParams getOrElse Nil)
  }
  def params(m: MethodInfo): String = {
    m.thisMethodSymbol map (x => ScalapView.methodParamsString(x)) getOrElse ""

    // paramsToString(
    //   if (m.paramNames.size != m.paramTypes.size) m.paramTypes
    //   else (m.paramNames, m.paramTypes).zipped map ((x, y) => "%s: %s".format(x, y))
    // )
  }
  def returnType(m: MethodInfo) = m.reflectReturnType match {
    case Some(t)  => javaTypeToString(t)
    case _        => "" + m.descriptor.returnType
    // case _        => asmTypeToString(asmReturnType(m.descriptor.text))
  }
  def throws(m: MethodInfo): String = ""  

  def signature(m: MethodInfo): String = spaceSepString(
    accessFlags(m.access),
    otherFlags(m.access),
    scalaFlags(m),
    "def",
    methodName(m) + methodSig(m),
    // "%s%s%s: %s".format(methodName(m), tparams(m), params(m), returnType(m)),
    throws(m)
  )
}
