/*
 * Tests for forgetful resolution.
 *
 */

package at.logic.algorithms.cutIntroduction

import org.specs2.mutable._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import scala.collection.mutable.HashMap
import at.logic.language.lambda.symbols._
import at.logic.language.hol.logicSymbols._
import at.logic.language.fol._
import CutIntroduction._

@RunWith(classOf[JUnitRunner])
class ForgetfulResolutionTest extends SpecificationWithJUnit {

  "Forgetful Resolution Should" should {
    
    "compute a single resolvent successfully" in {
      val a = Atom(new ConstantStringSymbol("A"), Nil)
      val b = Atom(new ConstantStringSymbol("B"), Nil)
      val c = Atom(new ConstantStringSymbol("C"), Nil)
      val d = Atom(new ConstantStringSymbol("D"), Nil)
      val e = Atom(new ConstantStringSymbol("E"), Nil)

      val f = And(And(Or(a,Or(b,c)), Or(Neg(b), d)), e)

      val res = ForgetfulResolve(f)

      //println("Formula (in CNF): " + f)
      //println("Resolvent: " + res)

      res.size must beEqualTo(1)
    }

    "improve the solution correctly" in {
      val p = at.logic.testing.LinearExampleProof(8)
      val ts = new FlatTermSet(TermsExtraction(p))
      val d = Decomposition(ts.termset)
      val ds = d.sortWith((d1,d2) => d1._1.length + d1._2.length < d2._1.length + d2._2.length )
      val decomp = ds(2)
      val ehs = new ExtendedHerbrandSequent(p.root, decomp, ts)
      val improv = improveSolution(ehs.canonicalSol, ehs)

      // TODO: type the expected value correctly
      //val expected = 
      //improv must
      success
    }
  }
}

