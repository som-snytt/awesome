package awesome
package jvm

import java.lang.reflect

class JavaClass(val clazz: JClass[_]) {
  private def oclass(cl: Class[_]): Class[_] = if (cl ne null) cl else NoClass
  private def omethod(m: JMethod): JMethod   = if (m ne null) m else NoMethod

  def methods         = uniqueList(clazz.getMethods, clazz.getDeclaredMethods)
  def constructors    = uniqueList(clazz.getConstructors, clazz.getDeclaredConstructors) filterNot (_.isSynthetic)
  def interfaces      = clazz.getGenericInterfaces
  def declaringClass  = oclass(clazz.getDeclaringClass)
  def enclosingClass  = oclass(clazz.getEnclosingClass)
  def enclosingMethod = omethod(clazz.getEnclosingMethod)

  lazy val descMap = methods groupBy (Descriptor getMethodDescriptor _)
  def findMethodInfo(minfo: MethodInfo): Option[reflect.Method] =
    (descMap get minfo.descriptor.text) flatMap { methods =>
      methods find (_.getName == minfo.name.toEncoded)
    }
}
