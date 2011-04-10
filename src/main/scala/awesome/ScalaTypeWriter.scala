package awesome

import scala.reflect.NameTransformer
import types.Base

trait TypeWriter[T, TArg] {
  def apply(tpe: T): String = apply(tpe, Nil)
  def apply(tpe: T, typeArgs: List[TArg]): String
}

abstract class ScalaTypeRewriter extends TypeWriter[String, String] {
  def rewrites: PartialFunction[String, String]

  val regexps = Map(
    """^scala\.collection\.mutable""" -> "mutable",
    """^scala\.collection\.immutable""" -> "immutable",
    """^scala\.collection\.generic""" -> "generic",
    """^java\.lang\.""" -> "",
    """^scala\.math\.""" -> "",
    """^scala\.""" -> ""
  )
  
  class ScalaName(name: String) {
    def toEncoded = NameTransformer encode name
    def toDecoded = NameTransformer decode name
    def toInternal = name.replace('.', '/')
    def toExternal = name.replace('/', '.')
  }
  
  def apply(tpe: String, typeArgs: List[String]): String = {
    (tpe indexOf '[') match {
      case -1   => ()
      case idx  => return apply(
        tpe take idx,
        tpe drop (idx + 1) stripSuffix "]" stripPrefix "[" split "," map (_.trim) toList
      ) // XXX
    }
    
    val name = NameTransformer decode tpe.replace('/', '.')
    
    // val (name, typeArgs) = (tpe indexOf '[') match {
    //   case -1   => (NameTransformer decode tpe.replace('/', '.'), args)
    //   case idx  =>
    //     assert(args.isEmpty)
    //     val _name = NameTransformer decode (tpe take idx).replace('/', '.')
    //     val _args = tpe drop (idx + 1) stripSuffix "]" stripPrefix "[" split "," map (_.trim)  // XXX
    //     
    //     (_name, _args.toList)
    // }
    def tpString = tparamsToString(typeArgs)
    def tp(x: Int) = typeArgs(x)

    def isObject          = name == "java.lang.Object" || name == "AnyRef"
    def isTuple           = name startsWith "scala.Tuple"
    def isFunction        = name startsWith "scala.Function"
    def isPartialFunction = name startsWith "scala.PartialFunction"
    def isCollection      = name startsWith "scala.collection."
    def isBoxed           = (name startsWith "java.lang.") && (Base.boxedNames contains name.stripPrefix("java.lang."))

    if (isObject) "AnyRef"
    else if (isTuple && typeArgs.nonEmpty) paramsToString(typeArgs map apply)
    else if (isFunction) typeArgs match {
      case Nil          => name
      case List(t1)     => "() => " + t1
      case List(t1, t2) => "%s => %s".format(t1, t2)
      case xs           => "%s => %s".format(paramsToString(xs.init), xs.last)
    }    
    else if (isPartialFunction && typeArgs.size == 2) "%s =>? %s".format(typeArgs: _*)
    else if (rewrites isDefinedAt name) rewrites(name) + tpString
    else if (isBoxed) name.replaceAll("""^java\.lang\.""", "jl.")
    else {
      val res = name + tpString
      for ((re, replacement) <- regexps) {
        val res2 = res.replaceFirst(re, replacement)
        if (res2 != res)
          return res2
      }
      res
    }
  }
}
