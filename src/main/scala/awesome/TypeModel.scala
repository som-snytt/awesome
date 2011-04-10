package awesome

trait TypeModel {
  type FlagsType
  type Name >: Null
  type Type >: Null

  val NoSymbol: Symbol
  val NoType: Type
  val NoPrefix: Type
  
  type Symbol >: Null
  type ClassSymbol <: Symbol
  type ModuleSymbol <: Symbol
  type MethodSymbol <: Symbol
  type TypeSymbol <: Symbol
  type AliasSymbol <: Symbol
  
  def symName(sym: Symbol): Name
  def symPath(sym: Symbol): Name
  def symPackage(sym: Symbol): Name
  
  def owner(sym: Symbol): Option[Symbol]
  def parent(sym: Symbol): Option[Symbol]
  def children(sym: Symbol): List[Symbol]
  
  def info(sym: Symbol): Type
  def supertypes(tpe: Type): List[Type]
  def typeParams(tpe: Type): List[Symbol]
  def typeArgs(tpe: Type): List[Type]

  def valueParams(tpe: Type): List[Symbol]
  def resultType(tpe: Type): Type
  def bounds(tpe: Type): (Type, Type)
  
  def methodSymbols(syms: List[Symbol]): List[MethodSymbol]
  def classSymbols(syms: List[Symbol]): List[ClassSymbol]
  def typeSymbols(syms: List[Symbol]): List[TypeSymbol]
  def aliasSymbols(syms: List[Symbol]): List[AliasSymbol]
  def moduleSymbols(syms: List[Symbol]): List[ModuleSymbol]
  
  def members(classSym: ClassSymbol): List[Symbol]
  def memberMethods(classSym: ClassSymbol): List[MethodSymbol]
  def memberClasses(classSym: ClassSymbol): List[ClassSymbol]
  def memberAliases(classSym: ClassSymbol): List[AliasSymbol]

  def findClass(name: String): ClassSymbol
  
  // trait TypeBounds extends Type {
  //   def lo: List[Type]
  //   def hi: List[Type]
  // }
  // trait TypeRef extends Type { 
  //   def pre: Type
  //   def sym: Symbol
  //   def args: List[Type]
  // }
  // trait TypeVar extends Type {
  //   def sym: Symbol
  //   def args: List[Type]
  // }
  // trait Wildcard extends Type { }
  // trait BoundedWildcard extends Type {
  //   def bounds: TypeBounds
  // }
  // trait ConstantType extends Type {
  //   def value: Any
  // }

  // abstract class TypeTraverser[T] {
  //   def traverse(tp: Type): Unit = tp match {
  //     case TypeBounds(lo, hi)       => lo foreach traverse ; hi foreach traverse
  //     case TypeRef(pre, sym, args)  => args foreach traverse
  //     case TypeVar(sym, args)       => args foreach traverse
  //     case BoundedWildcard(bounds)  => traverse(bounds)
  //     case ClassType(sym, members)  => 
  //     case MethodType(sym, tparams, params, resultType) => traverse(resultType)
  //     case ConstantType(value)      =>
  //   }
  // }
}
