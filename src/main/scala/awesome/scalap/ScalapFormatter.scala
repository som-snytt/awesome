package awesome
package scalap

import scala.tools.scalap.scalax.rules.{ scalasig => sp }
import ScalapModel._

class GenericScalaFormatter {  
  def supers(supers: List[String]): String = supers match {
    case Nil        => ""
    case x :: Nil   => " extends " + x
    case x :: xs    => " extends " + x + (xs map ("\n          with " + _) mkString)
  }
  def methods(methods: List[String]): String =
    methods.sorted map ("  " + _ + "\n") mkString
  
  def clasz(name: String, tparams: List[String], cons: String, sups: List[String], body: String): String = {
    "class %s%s%s%s {\n%s}\n".format(
      name,
      tparamsToString(tparams),
      cons,
      supers(sups),
      body
    )
  }
  
  def method(name: String, tparams: List[String], params: List[String], ret: String): String = {
    "def %s%s%s: %s".format(
      name,
      tparamsToString(tparams),
      paramsToString(params),
      ret
    )
  }
}

class AliasRewriter(aliasMap: Map[String, String], pkgName: String) extends PartialFunction[String, String] {
  private def rawName(s: String) = (s indexOf '[') match {
    case -1   => s
    case idx  => s take idx
  }
  private val rewrites = aliasMap map {
    case (k, v) => rawName(v) -> rawName(k)
  }  
  def isDefinedAt(str: String) = rewrites contains rawName(str)
  def apply(str: String) = {
    val alias = rewrites(rawName(str))
    alias + (str stripPrefix rawName(str))
  }
}

class ScalapFormatter {
  val generic = new GenericScalaFormatter { }
  val printer = new ScalapSigPrinter
  lazy val typeWriter = new ScalaTypeRewriter {
    val rewrites = new AliasRewriter(packageAliases("scala"), "scala")
  }
  import printer._tf

  def rawString(s: Symbol): String = printer toString s
  def rawString(t: Type): String = printer toString t
  
  def asString(s: Symbol): String = typeWriter(rawString(s))
  def asString(t: Type): String  = typeWriter(rawString(t))
  
  def isInitMethod(m: MethodSymbol) = m.name == "$init$"
  def filtMethods(classSym: ClassSymbol) = memberMethods(classSym) filterNot isInitMethod
  
  // def typeString(tpe: Type): String = typeWriter(tpe match {
  //   case sp.TypeRefType(pre, sym, args)   =>
  //     // val preString = if (pre == NoPrefix) "" else typeString(pre) + "#"
  //     val preString = ""
  //     val argString = tparamsToString(args map typeString)
  //     preString + symNameDecoded(sym) + argString
  //   case sp.TypeBoundsType(lo, hi)  =>
  //     val loString = typeString(lo)
  //     val hiString = typeString(hi)
  //     ">: " + lo + " <: " + hi
  // 
  //   case _  =>
  //     usePrinter(x => x.printType(tpe)(x._tf))
  // })
  // 
  
  def param(sym: Symbol): String = {
    symName(sym) + ": " + asString(info(sym))
  }

  def method(methodSym: MethodSymbol): String = {
    val tpe = methodSym.infoType
    
    generic.method(
      symNameDecoded(methodSym),
      typeParams(tpe) map asString,
      valueParams(tpe) map param,
      asString(resultType(tpe))
    )
  }
    
  def clasz(classSym: ClassSymbol): String = clasz(classSym, Nil)
  def clasz(classSym: ClassSymbol, supers: List[ClassSymbol]): String = {
    def polySplit(tpe: Type) = tpe match {
      case sp.PolyType(ct, syms)                => (ct, syms.toList, None)
      case sp.PolyTypeWithCons(ct, syms, con)   => (ct, syms.toList, Some(con))
      case _                                    => (tpe, Nil, None)
    }
    
    val (classTpe, tparams, constructor) = polySplit(classSym.infoType)
    val cs       = memberClasses(classSym)
    
    val body = {
      (classSym :: supers) map { sym =>
        "  // defined in " + symNameDecoded(sym) + "\n" + generic.methods(filtMethods(sym) map method) + "\n"
      } mkString ""
    }
    
    generic.clasz(
      symNameDecoded(classSym),
      tparams map asString,
      constructor getOrElse "",
      supertypes(classTpe) map asString,
      body
    )
  }
}
