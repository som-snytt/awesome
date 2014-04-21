package awesome
package jvm

import java.lang.reflect
import attr.{ InnerClasses, Exceptions, ScalaSig, EnclosingMethod }
import pickler.{ ScalaSigParser }
import awesome.scalap.ScalaSigFinder
import scala.reflect.NameTransformer

// object CopiedScalaSigPrinter {
//   import scala.tools.scalap.scalax.rules.scalasig.{ ScalaSigParser => _, _ }
//
//   def typeToString(t: Type): String = {
//     // print type itself
//     t match {
//       case ThisType(symbol) => sep + processName(symbol.path) + ".type"
//       case SingleType(pre, sym) => sep + processName(symbol.path) + ".type"
//       case ConstantType(constant) => "Constant(" + constant.toString + ")"
//       case TypeRefType(pre, sym, args) =>
//
//       sep + (symbol.path match {
//         case "scala.<repeated>" => flags match {
//           case TypeFlags(true) => toString(typeArgs.head) + "*"
//           case _ => "scala.Seq" + typeArgString(typeArgs)
//         }
//         case "scala.<byname>" => "=> " + toString(typeArgs.head)
//         case _ => {
//           val path = StringUtil.cutSubstring(symbol.path)(".package") //remove package object reference
//           StringUtil.trimStart(processName(path) + typeArgString(typeArgs), "<empty>.")
//         }
//       })
//       case TypeBoundsType(lower, upper) => {
//         val lb = toString(lower)
//         val ub = toString(upper)
//         val lbs = if (!lb.equals("scala.Nothing")) " >: " + lb else ""
//         val ubs = if (!ub.equals("scala.Any")) " <: " + ub else ""
//         lbs + ubs
//       }
//       case RefinedType(classSym, typeRefs) => sep + typeRefs.map(toString).mkString("", " with ", "")
//       case ClassInfoType(symbol, typeRefs) => sep + typeRefs.map(toString).mkString(" extends ", " with ", "")
//       case ClassInfoTypeWithCons(symbol, typeRefs, cons) => sep + typeRefs.map(toString).
//               mkString(cons + " extends ", " with ", "")
//
//       case ImplicitMethodType(resultType, _) => toString(resultType, sep)
//       case MethodType(resultType, _) => toString(resultType, sep)
//
//       case PolyType(typeRef, symbols) => typeParamString(symbols) + toString(typeRef, sep)
//       case PolyTypeWithCons(typeRef, symbols, cons) => typeParamString(symbols) + processName(cons) + toString(typeRef, sep)
//       case AnnotatedType(typeRef, attribTreeRefs) => {
//         toString(typeRef, sep)
//       }
//       case AnnotatedWithSelfType(typeRef, symbol, attribTreeRefs) => toString(typeRef, sep)
//       //case DeBruijnIndexType(typeLevel, typeIndex) =>
//       case ExistentialType(typeRef, symbols) => {
//         val refs = symbols.map(toString _).filter(!_.startsWith("_")).map("type " + _)
//         toString(typeRef, sep) + (if (refs.size > 0) refs.mkString(" forSome {", "; ", "}") else "")
//       }
//       case _ => sep + t.toString
//     }
//   }
//
//   def getVariance(t: TypeSymbol) = if (t.isCovariant) "+" else if (t.isContravariant) "-" else ""
//
// }

object ScalapView {
  import scala.tools.scalap.scalax.rules.scalasig.{ ScalaSigParser => _, _ }

  def stripPrivatePrefix(name: String) =
    name.replaceAll("""^.*\Q$$\E""", "")

  def processName(name: String) =
    NameTransformer.decode(stripPrivatePrefix(name))

  def symString(sym: Symbol) =
    processName(sym.path)

  def tpString(tp: Type): String = tp match {
    case ThisType(sym)                => symString(sym) + ".type"
    case SingleType(tref, sym)        => symString(sym) + ".type"
    case ConstantType(constant)       => constant.asInstanceOf[AnyRef].getClass.getName
    case TypeRefType(pre, sym, args)  => symString(sym) + typeArgs(args)
    case TypeBoundsType(lo, hi)       =>
      val lb = tpString(lo) match {
        case "scala.Nothing"  => ""
        case x                => " >: " + x
      }
      val ub = tpString(hi) match {
        case "scala.Any"      => ""
        case x                => " <: " + x
      }
      lb + ub
    case PolyType(mt, syms)           =>
      typeParams(syms) + tpString(mt)
    // case mt @ ImplicitMethodType(resType, params) =>
    //   val ps = methodSymbols(params) map (x => x.name + ": " + tpString(x.infoType))
    //   ps.mkString("(implicit ", ", ", "): " + tpString(resType))
    case mt @ MethodType(resType, params)  =>
      val ps = methodSymbols(params) map (x => x.name + ": " + tpString(x.infoType))
      ps.mkString("(", ", ", "): " + tpString(resType))

    case x => "TODO: " + x.getClass
    // case class RefinedType(classSym : Symbol, typeRefs : List[Type]) extends Type
    // case class ClassInfoType(symbol : Symbol, typeRefs : Seq[Type]) extends Type
    // case class ClassInfoTypeWithCons(symbol : Symbol, typeRefs : Seq[Type], cons: String) extends Type
    // case class PolyTypeWithCons(typeRef : Type, symbols : Seq[TypeSymbol], cons: String) extends Type
    // case class ImplicitMethodType(resultType : Type, paramSymbols : Seq[Symbol]) extends Type
    // case class AnnotatedType(typeRef : Type, attribTreeRefs : List[Int]) extends Type
    // case class AnnotatedWithSelfType(typeRef : Type, symbol : Symbol, attribTreeRefs : List[Int]) extends Type
    // case class DeBruijnIndexType(typeLevel : Int, typeIndex : Int) extends Type
    // case class ExistentialType(typeRef : Type, symbols : Seq[Symbol]) extends Type
  }

  def methodSymbols(xs: Seq[Symbol]): List[MethodSymbol] =
    xs.toList collect { case x: MethodSymbol => x }

  def typeSymbols(xs: Seq[Symbol]): List[TypeSymbol] =
    xs.toList collect { case x: TypeSymbol => x }

  def typeArgs(tps: Seq[Type]): String = {
     tparamsToString(tps map tpString)
  }

  def typeParams(syms: Seq[Symbol]): String = {
    tparamsToString(typeSymbols(syms) map typeParam)
  }

  def typeParam(x: TypeSymbol): String = {
    val variance =
      if (x.isCovariant) "+"
      else if (x.isContravariant) "-"
      else ""

    variance + x.name
  }

  def typeParamsString(m: MethodSymbol): String = {
    tparamsToString(typeSymbols(m.children) map typeParam)
  }

  def methodParamsString(x: MethodSymbol): String = {
    tpString(x.infoType)
    // val ps = methodSymbols(x.children) filter (_.isParam)
    //
    // paramsToString(methodSymbols(x.children) filter (_.isParam)
    //
    // val infoType = x.infoType
    // x.name + ": " + tpString(infoType)
  }
}

trait ScalaSigAnnotated {
  import scala.tools.scalap.scalax.rules.scalasig.{ ScalaSigParser => _, _ }

  def className: String

  def scalaSigBytes        = ScalaSigFinder.nameToBytes(className)
  def scalaSigPickleBuffer = ScalaSigFinder.nameToPickleBuffer(className)
  def scalapScalaSig       = ScalaSigFinder.fromName(className)
  def scalapSymbols        = scalapScalaSig.toList flatMap (_.symbols)

  def thisClassSymbol  = classSymbols find (_.path == className)
  def classSymbols     = scalapSymbols collect { case x: ClassSymbol => x }
  def objectSymbols    = scalapSymbols collect { case x: ObjectSymbol => x }
  def methodSymbols    = scalapSymbols collect { case x: MethodSymbol => x }
  def typeParamSymbols = scalapSymbols collect { case x: TypeSymbol => x }
  def aliasSymbols     = scalapSymbols collect { case x: AliasSymbol => x }

  def methodSymbolFor(id: Ident) = methodSymbols find (_.name.toDecoded == id.toDecoded)
  def classTypeParamSymbols = thisClassSymbol.toList flatMap (_.children) collect {
    case x: TypeSymbol => x
  }

  lazy val scalaSig = scalaSigBytes flatMap (x => ScalaSigParser(x).toOption)
  def isScala = scalapScalaSig.isDefined
}

case class ClassInfo(
  access: Flags,
  id: Ident,
  superId: Ident,
  interfaces: List[Ident],
  attributes: List[Attribute]
) extends Member with ScalaSigAnnotated {

  type SigType = ClassSignature
  def createSignature = x => ClassSignature(x, name.toScalaString)

  lazy val clazz: JClass[_] = name.clazz
  lazy val companion = ClassFileParser(name.toCompanion).get.process
  def clazzName = clazz.getName
  def className = name.toExternal

  def isScalaObject = isScala && (name endsWith "$")
  def enclosingMethod = findAttr({ case x: EnclosingMethod => x })

  private lazy val jclass = new JavaClass(clazz)
  lazy val reflectMethods = jclass.methods
  lazy val reflectConstructors = jclass.constructors
  lazy val reflectInterfaces = jclass.interfaces
  lazy val reflectEnclosingClass = jclass.enclosingClass
  lazy val reflectEnclosingMethod = jclass.enclosingMethod
  lazy val reflectDeclaringClass = jclass.declaringClass

  // def declaringClass: Option[ClassInfo] = {
  //   // if this is primitive None
  //   // if this has no inner class attribute => None
  //
  // }

  /** These methods mimic the java reflection implementation logic.
   *  See doc/jvm.cpp and other files in doc/ for details.
   */
  def enclosingClass: Option[JClass[_]] = {
    // There are five kinds of classes (or interfaces):
    // a) Top level classes
    // b) Nested classes (static member classes)
    // c) Inner classes (non-static member classes)
    // d) Local classes (named classes declared within a method)
    // e) Anonymous classes
    //
    // JVM Spec 4.8.6: A class must have an EnclosingMethod
    // attribute if and only if it is a local class or an
    // anonymous class.
    reflectEnclosingMethod match {
      // This is a top level or a nested class or an inner class (a, b, or c)
      case None     => reflectDeclaringClass
      // This is a local class or an anonymous class (d or e)
      case Some(m)  => reflectEnclosingClass match {
        case None | Some(`clazz`) => sys.error("Malformed enclosing method information")
        case x                      => x
      }
    }
  }

  def simpleName =
    if (clazz.isArray) clazz.getComponentType.getSimpleName + "[]"
    else simpleBinaryName match {
      case None   => clazzName drop ((clazzName lastIndexOf '.') + 1) // strip package
      // According to JLS3 "Binary Compatibility" (13.1) the binary
      // name of non-package classes (not top level) is the binary
      // name of the immediately enclosing class followed by a '$' followed by:
      // (for nested and inner classes): the simple name.
      // (for local classes): 1 or more digits followed by the simple name.
      // (for anonymous classes): 1 or more digits.

      // Since getSimpleBinaryName() will strip the binary name of
      // the immediatly enclosing class, we are now looking at a
      // string that matches the regular expression "\$[0-9]*"
      // followed by a simple name (considering the simple of an
      // anonymous class to be the empty string).

      case Some(str) =>
        if (str == "" || str(0) != '$') sys.error(s"Malformed class name: $clazzName")
        // This ends up as the empty string iff this is an anonymous class
        else str.tail dropWhile (ch => '0' <= ch && ch <= '9')
  }

  def canonicalName =
    if (clazz.isArray) Option(clazz.getComponentType.getCanonicalName) map (_ + "[]")
    else if (isLocalOrAnonymousClass) None
    else enclosingClass match {
      case None     => clazzName
      case Some(ec) => Option(ec.getCanonicalName) map (_ + "." + simpleName)
    }

  def isTopLevel = enclosingClass.isEmpty
  def isNestedClass = enclosingClass.isDefined

  // These are direct translations of the java implementations.
  def isAnonymousClass = try simpleName == "" catch { case _: RuntimeException => false }
  def isLocalClass     = isLocalOrAnonymousClass && !isAnonymousClass
  def isMemberClass    = simpleBinaryName.isDefined && !isLocalOrAnonymousClass

  private def isLocalOrAnonymousClass = enclosingMethod.isDefined
  private def simpleBinaryName        = enclosingClass map (x => clazz.getName drop x.getName.length)

  private def validateEnclosingMethod =
    if (enclosingMethod.isDefined == (isLocalClass || isAnonymousClass)) Nil
    else List("A class must have an EnclosingMethod attribute if and only if it is a local class or an anonymous class.")

  lazy val validationFailures: List[String] = List(
    validateEnclosingMethod
  ).flatten

  def reflectMethodFor(m: MethodInfo) = jclass findMethodInfo m

  private def indent(s: String, num: Int = 8) = (" " * num) + s
  private def indentln(s: String) = indent(s) + "\n"

  def whatString = {
    def adj(cond: Boolean, word: String): String = if (cond) word else ""
    val noun =
      if (access.isInterface)
        if (isScala) "trait" else "interface"
      else
        if (isScalaObject) "object" else "class"

    spaceSepString(
      adj(isAnonymousClass, "anonymous"),
      adj(isLocalClass, "local"),
      adj(isMemberClass, "inner"),
      noun
    )
  }

  def nameString    = javaTypeToString(clazz)
  def extendsStr    = clazz.getGenericSuperclass match {
    case null => ""
    case x    => indent("extends " + javaTypeToString(x), 5) + "\n"
  }
  def interfaceStr  = reflectInterfaces map (x => indent("with " + javaTypeToString(x))) mkString "\n"

  override def toString = spaceSepString(
    accessString,
    whatString,
    nameString + "\n" + extendsStr + interfaceStr
  )
}
