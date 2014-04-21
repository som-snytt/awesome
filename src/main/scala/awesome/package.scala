import awesome.io.{ Jar, Jars }

package object awesome extends pkg.Constants
                          with pkg.Types
                          with pkg.Utility
                          // with pkg.ASM
                          with pkg.Pimps  {

  implicit val identOrdering: Ordering[Ident] = Ordering[String] on (_.name)
  implicit def string2ident(s: String): Ident = Ident(s)

  val NoClass  = classOf[awesome.NoClass]
  val NoMethod = NoClass.getDeclaredMethods.head
  val NoName   = "<none>"

  implicit class jClassOps(val clazz: Class[_]) {
    def isEmpty   = clazz eq NoClass
    def isDefined = !isEmpty
  }
  implicit class jMethodOps(val m: JMethod) {
    def isEmpty   = m eq NoMethod
    def isDefined = !isEmpty
  }
}

package awesome {
  class NoClass {
    def noMethod(): Unit = ()
  }
}
