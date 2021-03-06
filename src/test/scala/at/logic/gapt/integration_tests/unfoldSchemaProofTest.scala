
package at.logic.gapt.integration_tests

import java.io.InputStreamReader

import at.logic.gapt.formats.shlk_parsing.SHLK.parseProof
import at.logic.gapt.language.schema._
import org.specs2.execute.Success
import org.specs2.mutable._

// Moved this test to integration_tests because it uses an external file.
class UnfoldSchemaProofTest extends Specification {
  //implicit val factory = defaultFormulaOccurrenceFactory
  "UnfoldSchemaProofTest" should {
    "unfold the adder.slk" in {
      val zero = IntZero(); val one = Succ( IntZero() ); val two = Succ( Succ( IntZero() ) ); val three = Succ( Succ( Succ( IntZero() ) ) )
      val str = new InputStreamReader( getClass.getClassLoader.getResourceAsStream( "schema-adder.lks" ) )
      val map = parseProof( str )
      val n = IntVar( "n" ); val n1 = Succ( n ); val n2 = Succ( n1 ); val n3 = Succ( n2 )
      val k = IntVar( "k" ); val i = IntVar( "i" )
      val A3 = IndexedPredicate( "A", three )
      val A = IndexedPredicate( "A", i )
      val An3 = IndexedPredicate( "A", n3 )
      val An1 = IndexedPredicate( "A", n1 )
      val b = BigAnd( i, A, zero, n3 )
      val subst = SchemaSubstitution( ( k, two ) :: Nil )

      Success()
    }
  }
}

