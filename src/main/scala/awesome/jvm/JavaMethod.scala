package awesome
package jvm

import java.lang.reflect
import reflect.{ GenericArrayType, Type }

class JavaMethod(val method: reflect.Method) {
  def isVarArgs = method.isVarArgs
  def varArgType = {
    if (argumentTypes.isEmpty) None
    else argumentTypes.last match {
      case x: GenericArrayType if isVarArgs => Some(x.getGenericComponentType)
      case x: JClass[_] if x.isArray        => Some(x.getComponentType)
      case _                                => None
    }
  }
  
  lazy val typeParams    = method.getTypeParameters.toList
  lazy val argumentTypes = method.getGenericParameterTypes.toList
  lazy val returnType    = method.getGenericReturnType
  lazy val methodName    = method.getName
}
