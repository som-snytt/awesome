package awesome
package scalap

import scala.tools.scalap._
import scalax.rules.{ scalasig => sp }
import scala.reflect.NameTransformer
import java.io.{ PrintStream, ByteArrayOutputStream }

object ScalapModel extends ScalapModel {
  private val cache = new HashMap[String, ClassSymbol]
  private val formatter = new ScalapFormatter
  import formatter.rawString

  def packageAliases(pkgName: String): Map[String, String] = {
    val classSym = findClass(pkgName + ".package")
    val pkg      = symPackage(classSym)
    val pairs    = for (alias <- memberAliases(classSym)) yield {
      alias.infoType match {
        case sp.PolyType(tpe, targs) =>
          (alias.name + tparamsToString(targs map rawString), rawString(tpe))
        case _                          =>
          (alias.name, rawString(alias.infoType))
      }
    }

    pairs.toMap
  }

  def classNamePrettyPrint(className: String): String =
    formatter.clasz(findClass(className), findSupers(className))

  def findSupers(className: String): List[ClassSymbol] = {
    linearization(findClass(className))
    //
    // val sym    = findClass(className)
    // val supers = supertypes(sym.infoType) collect {
    //   case sp.TypeRefType(pre, sym, args)       => sym
    //   case sp.ClassInfoType(sym, supers)        => sym
    //   case sp.ClassInfoTypeWithCons(sym, _, _)  => sym
    // }
    // supers map (x => findClass(x.toString)) filterNot (_ == null)
  }

  override def findClass(className: String): ClassSymbol =
    cache.getOrElseUpdate(className, super.findClass(className))
}

class ScalapModel extends TypeModel {
  type FlagsType = Int
  type Name = String
  type Type = sp.Type
  type Symbol = sp.Symbol
  type ClassSymbol = sp.ClassSymbol
  type MethodSymbol = sp.MethodSymbol
  type TypeSymbol = sp.TypeSymbol
  type AliasSymbol = sp.AliasSymbol
  type ModuleSymbol = sp.ObjectSymbol

  val NoSymbol = sp.NoSymbol
  val NoType = sp.NoType
  val NoPrefix = sp.NoPrefixType

  val byteStream = new ByteArrayOutputStream()
  val outStream = new PrintStream(byteStream)
  val sigPrinter = new sp.ScalaSigPrinter(outStream, true)
  def usePrinter[T](f: sp.ScalaSigPrinter => Unit): String = {
    try {
      f(sigPrinter)
      outStream.flush()
      byteStream.toString
    }
    finally {
      byteStream.reset()
    }
  }

  def symNameDecoded(sym: Symbol): Name = NameTransformer decode sym.name
  def symName(sym: Symbol): Name = sym.name
  def symPath(sym: Symbol): Name = sym.path
  def symPackage(sym: Symbol): Name = symPath(sym) split '.' dropRight 1 mkString "."

  def owner(sym: Symbol): Option[Symbol] = sym match {
    case x: sp.SymbolInfoSymbol => Some(x.symbolInfo.owner)
    case _                      => None
  }
  def parent(sym: Symbol): Option[Symbol] = sym.parent
  def children(sym: Symbol): List[Symbol] = sym.children.toList
  def info(sym: Symbol): Type = sym match {
    case x: sp.SymbolInfoSymbol => x.infoType
    case _                      => NoType
  }
  def getSymbol(tpe: Type): Symbol = tpe match {
    case sp.ThisType(sym)    => sym
    case sp.SingleType(_, sym) => sym
    case sp.TypeRefType(_, sym, _) => sym
    case sp.RefinedType(sym, _)  => sym
    case sp.ClassInfoType(sym, _) => sym
    case sp.ClassInfoTypeWithCons(sym, _, _) => sym
    case _                          => NoSymbol
  }

  def linearization(classSym: ClassSymbol): List[ClassSymbol] = {
    def parents(sym: ClassSymbol) = supertypes(sym.infoType) collect {
      case sp.TypeRefType(pre, sym, args)       => sym
      case sp.ClassInfoType(sym, supers)        => sym
      case sp.ClassInfoTypeWithCons(sym, _, _)  => sym
    } map (x => findClass(x.toString)) filterNot (_ == null)

    def loop(in: List[ClassSymbol], out: List[ClassSymbol]): List[ClassSymbol] = {
      if (in.isEmpty) out
      else {
        val newParents = in flatMap parents distinct
        val newIn = newParents filterNot (out contains _)
        loop(newIn, out ++ in distinct)
      }
    }

    loop(List(classSym), Nil)
  }
  def supertypes(tpe: Type): List[Type] = tpe match {
    case sp.PolyType(ct, _)                       => supertypes(ct)
    case sp.PolyTypeWithCons(ct, _, _)            => supertypes(ct)
    case sp.ClassInfoType(_, typeRefs)            => typeRefs.toList ++ (typeRefs.toList flatMap supertypes)
    case sp.ClassInfoTypeWithCons(_, typeRefs, _) => typeRefs.toList ++ (typeRefs.toList flatMap supertypes)
    case _                                        => Nil
  }
  def typeParams(tpe: Type): List[Symbol] = tpe match {
    case sp.PolyType(_, syms)             => syms.toList
    case sp.PolyTypeWithCons(_, syms, _)  => syms.toList
    case _                                => Nil
  }
  def typeArgs(tpe: Type): List[Type] = tpe match {
    case sp.TypeRefType(pre, sym, args)  => args.toList
    case _                            => Nil
  }
  def valueParams(tpe: Type): List[Symbol] = tpe match {
    // case sp.ImplicitMethodType(_, params)  => params.toList
    case sp.MethodType(_, params)          => params.toList
    case _                              => Nil
  }
  def resultType(tpe: Type): Type = tpe match {
    case sp.MethodType(result, _)              => result
    // case sp.ImplicitMethodType(result, _)      => result
    case sp.PolyType(typeRef, _)               => resultType(typeRef)
    case sp.PolyTypeWithCons(typeRef, _, _)    => resultType(typeRef)
    case _                                  => tpe
  }

  def bounds(tpe: Type): (Type, Type) = tpe match {
    case sp.TypeBoundsType(lo, hi)   => (lo, hi)
    case _                        => (NoType, NoType)
  }

  def methodSymbols(syms: List[Symbol]): List[MethodSymbol] =
    syms collect { case x: sp.MethodSymbol => x }

  def classSymbols(syms: List[Symbol]): List[ClassSymbol] =
    syms collect { case x: sp.ClassSymbol => x }

  def typeSymbols(syms: List[Symbol]): List[TypeSymbol] =
    syms collect { case x: sp.TypeSymbol => x }

  def aliasSymbols(syms: List[Symbol]): List[AliasSymbol] =
    syms collect { case x: sp.AliasSymbol => x }

  def moduleSymbols(syms: List[Symbol]): List[ModuleSymbol] =
    syms collect { case x: sp.ObjectSymbol => x }

  def members(classSym: ClassSymbol): List[Symbol] =
    children(classSym)

  def memberMethods(classSym: ClassSymbol): List[MethodSymbol] =
    methodSymbols(members(classSym))

  def memberClasses(classSym: ClassSymbol): List[ClassSymbol] =
    classSymbols(members(classSym))

  def memberAliases(classSym: ClassSymbol): List[AliasSymbol] =
    aliasSymbols(members(classSym))

  def findClass(className: String): ClassSymbol = {
    val simpleName = className split '.' last
    val ssig = ScalaSigFinder.fromName(className) getOrElse { return null }
    val syms = classSymbols(ssig.symbols.toList)
    syms find (_.name == simpleName) getOrElse null
  }

  private def stripPrivatePrefix(name: String) =
    name.replaceAll("""^.*\Q$$\E""", "")

  private def processName(name: String) =
    NameTransformer.decode(stripPrivatePrefix(name))

  def symbolsForClassName(className: String) = {
    ScalaSigFinder.fromName(className).toList flatMap (_.symbols)
  }

  // case SymbolInfo(name, owner, flags, privateWithin, info, entry) =>
  // case ExternalSymbol(name, parent, entry)                        =>
  // case TypeSymbol(info)                                           =>
  // case AliasSymbol(info)                                          =>
  // case ClassSymbol(info, thisType)                                =>
  // case ObjectSymbol(info)                                         =>
  // case MethodSymbol(info, aliasRef)                               =>
  //
  // case ThisType(sym)                                   =>
  // case SingleType(typeRef, sym)                        =>
  // case ConstantType(constant)                          =>
  // case TypeRefType(prefix, sym, typeArgs)              =>
  // case TypeBoundsType(lower, upper)                    =>
  // case RefinedType(caseSym, typeRefs)                  =>
  // case ClassInfoType(sym, typeRefs)                    =>
  // case ClassInfoTypeWithCons(sym, typeRefs, cons)      =>
  // case MethodType(resultType, params)                  =>
  // case PolyType(typeRef, syms)                         =>
  // case PolyTypeWithCons(typeRef, syms, cons)           =>
  // case ImplicitMethodType(resultType, params)          =>
  // case AnnotatedType(typeRef, attribRefs)              =>
  // case AnnotatedWithSelfType(typeRef, sym, attribRefs) =>
  // case DeBruijnIndexType(typeLevel, typeIndex)         =>
  // case ExistentialType(typeRef, syms)                  =>
}

// trait ScalaSigAnnotated {
//   import scala.tools.scalap.scalax.rules.scalasig.{ ScalaSigParser => _, _ }
//
//   def className: String
//
//   def scalaSigBytes        = ScalaSigFinder.nameToBytes(className)
//   def scalaSigPickleBuffer = ScalaSigFinder.nameToPickleBuffer(className)
//   def scalapScalaSig       = ScalaSigFinder.fromName(className)
//   def scalapSymbols        = scalapScalaSig.toList flatMap (_.symbols)
//
//   def thisClassSymbol  = classSymbols find (_.path == className)
//   def classSymbols     = scalapSymbols collect { case x: ClassSymbol => x }
//   def objectSymbols    = scalapSymbols collect { case x: ObjectSymbol => x }
//   def methodSymbols    = scalapSymbols collect { case x: MethodSymbol => x }
//   def typeParamSymbols = scalapSymbols collect { case x: TypeSymbol => x }
//   def aliasSymbols     = scalapSymbols collect { case x: AliasSymbol => x }
//
//   def methodSymbolFor(id: Ident) = methodSymbols find (_.name.toDecoded == id.toDecoded)
//   def classTypeParamSymbols = thisClassSymbol.toList flatMap (_.children) collect {
//     case x: TypeSymbol => x
//   }
//
//   lazy val scalaSig = scalaSigBytes flatMap (x => ScalaSigParser(x).toOption)
//   def isScala = scalapScalaSig.isDefined
// }
