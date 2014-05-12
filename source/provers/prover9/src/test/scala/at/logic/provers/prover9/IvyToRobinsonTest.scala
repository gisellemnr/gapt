package at.logic.parsing.ivy

import conversion.IvyToRobinson
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.mutable.SpecificationWithJUnit
import at.logic.parsing.lisp
import java.io.File.separator
import util.parsing.input.Reader
import lisp.{SExpressionParser}

/**
 * Test for the Ivy interface.
 * FIXME: all tests are skipped
 */
@RunWith(classOf[JUnitRunner])
class IvyToRobinsonTest extends SpecificationWithJUnit {

  "The Ivy Parser " should {
    " parse the test files factor.ivy and factor2.ivy " in {
      skipped("file missing")
      try {
        val result = SExpressionParser("target" + separator + "test-classes" + separator +"factor.ivy")
        result must not beEmpty
        val proof = result.head
        proof match {
          case lisp.List(_) =>
            val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
            val rinput = IvyToRobinson(pinput)

          case _ =>
            "The proof in factor.ivy must have some inferences" must beEqualTo("failed")
        }

        val result2 = SExpressionParser("target" + separator + "test-classes" + separator +"factor2.ivy")
        result2 must not beEmpty
        val proof2 = result2.head
        proof2 match {
          case lisp.List(_) =>
            val pinput = IvyParser.parse(proof2, IvyParser.is_ivy_variable)
            val rinput = IvyToRobinson(pinput)

          case _ =>
            "The proof in factor.ivy must have some inferences" must beEqualTo("failed")
        }
        (true) must beEqualTo(true)
      } catch {
        case e:Exception =>
          "" must beEqualTo("Exception occurred during proof parsing! "+e.getMessage + "\n"+
            e.getStackTrace().toList.foldLeft("")((str:String, el : StackTraceElement) => str + "\n" + el.toString)  )

      }
    }


    " parse the test file manyliterals.ivy " in {
      skipped("file missing")
      try {
        val result = SExpressionParser("target" + separator + "test-classes" + separator +"manyliterals.ivy")
        result must not beEmpty
        val proof = result.head
        proof match {
          case lisp.List(_) =>
            val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
            val rinput = IvyToRobinson(pinput)

          case _ =>
            "The proof in manyliterals.ivy must have some inferences" must beEqualTo("failed")
        }
        (true) must beEqualTo(true)

      } catch {
        case e:Exception =>
          "" must beEqualTo("Exception occurred during proof parsing! "+e.getMessage + " || "+
            e.getStackTrace().toList.foldLeft("")((str:String, el : StackTraceElement) => str + "\n" + el.toString)  )

      }
    }

    " parse the test file simple2.ivy " in {
      skipped("file missing")
      try {
        val result = SExpressionParser("target" + separator + "test-classes" + separator +"simple2.ivy")
        true must beEqualTo(true)
      } catch {
        case e:Exception =>
          true must beEqualTo(false)

      }
    }
  }

  " parse the test file prime1-0sk.ivy (clause set of the 0 instance of the prime proof) " in {
    skipped("paramodulation still has problems")
    try {
      val result = SExpressionParser("target" + separator + "test-classes" + separator +"prime1-0sk.ivy")
      result must not beEmpty
      val proof = result.head
      proof match {
        case lisp.List(_) =>
          val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
          val rinput = IvyToRobinson(pinput)

        case _ =>
          "The proof in prime1-0sk.ivy must have some inferences" must beEqualTo("failed")
      }
      (true) must beEqualTo(true)
    } catch {
      case e:Exception =>
        true must beEqualTo(false)

    }
  }

  " parse the test file GRA014+1.ivy " in {
    skipped("file missing")
    try {
      val result = SExpressionParser("target" + separator + "test-classes" + separator +"GRA014+1.ivy")
      result must not beEmpty
      val proof = result.head
      proof match {
        case lisp.List(_) =>
          val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
          val rinput = IvyToRobinson(pinput)

        case _ =>
          "The proof in manyliterals.ivy must have some inferences" must beEqualTo("failed")
      }
      (true) must beEqualTo(true)

    } catch {
      case e:Exception =>
        true must beEqualTo(false)

    }
  }

  " parse the test file GEO037-2.ivy " in {
    skipped("file missing")
    try {
      val result = SExpressionParser("target" + separator + "test-classes" + separator +"GEO037-2.ivy")
      result must not beEmpty
      val proof = result.head
      proof match {
        case lisp.List(_) =>
          val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
          val rinput = IvyToRobinson(pinput)

        case _ =>
          "The proof in GEO037-2.ivy must have some inferences" must beEqualTo("failed")
      }
      (true) must beEqualTo(true)

    } catch {
      case e:Exception =>
        true must beEqualTo(false)

    }
  }

  " parse the test file issue221.ivy " in {
    skipped("file missing")
    try {
      val result = SExpressionParser("target" + separator + "test-classes" + separator +"issue221.ivy")
      result must not beEmpty
      val proof = result.head
      proof match {
        case lisp.List(_) =>
          val pinput = IvyParser.parse(proof, IvyParser.is_ivy_variable)
          val rinput = IvyToRobinson(pinput)

        case _ =>
          "The proof in issue221.ivy must have some inferences" must beEqualTo("failed")
      }
      (true) must beEqualTo(true)
    } catch {
      case e:Exception =>
        true must beEqualTo(false)

    }
  }
}

