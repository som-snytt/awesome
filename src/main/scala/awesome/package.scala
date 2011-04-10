import awesome.io.{ Jar, Jars }

package object awesome extends pkg.Constants
                          with pkg.Types 
                          with pkg.Utility
                          // with pkg.ASM 
                          with pkg.Pimps  {
  
  implicit val identOrdering: Ordering[Ident] = Ordering[String] on (_.name)
  implicit def string2ident(s: String): Ident = Ident(s)
  
  // 2.8/2.9 compat
  object sys {
    def error(message: String): Nothing = throw new RuntimeException(message)
  }
}