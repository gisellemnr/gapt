/*
 * abstractTypedLambdaCalculus.scala
 *
 */

package at.logic.language.lambda

import scala.util.parsing.combinator._

package types {
  abstract class TA {
    def ->(that:TA) = new ->(this, that)
  }
  abstract class TAtomicA extends TA
  abstract class TComplexA extends TA
  object TAtomicA {
    def unapply(ta: TA) = ta match {
      case Tindex() => Some(ta)
      case Ti() => Some(ta)
      case To() => Some(ta)
      case _ => None
    }
  }
  case class Tindex() extends TAtomicA {override def toString = "ind"}//for indexed propositions. Look at schemata.
  case class Ti() extends TAtomicA {override def toString = "i"}
  case class To() extends TAtomicA {override def toString = "o"}
  case class ->(in:TA, out:TA) extends TComplexA {override def toString = "(" + in.toString + "->" + out.toString + ")"}

  object Definitions {
    def i = Ti()
    def o = To()
    def ind = Tindex()
  }

  // convenience factory to create function types
  // with argument types from and return type to
  object FunctionType {
    def apply(to: TA, from: List[TA]) : TA = if (!from.isEmpty) (from :\ to)(->) else to
    def unapply(ta: TA): Option[Pair[TA, List[TA]]] = ta match {
      case TAtomicA(_) => Some(ta, Nil)
      case ->(t1, TAtomicA(t2)) => Some(t2, t1::Nil)
      case ->(t1, t2) => {
        unapply(t2) match {
          case Some((ta, ls)) => Some(ta, t1::ls)
          case None => None
        }
      }
      case _ => None
    }
  }

  object  StringExtractor {
    def apply(t:TA):String = t match {
      case Ti() => "i"
      case To() => "o"
      case ->(in,out) => "("+ apply(in) + " -> " + apply(out) +")"
    }
    def unapply(s:String):Option[TA] = {
      val p = new JavaTokenParsers with Parsers
      p.parseAll(p.Type,s) match {
        case p.Success(result,_) => Some(result)
        case _ => None
      }
    }
  }

  object ImplicitConverters {
    implicit def fromString(s:String):TA = StringExtractor.unapply(s) match {
      case Some(result) => result
      case None =>  throw new IllegalArgumentException("Bad syntax for types: "+s)
    }
  }

  trait Parsers extends JavaTokenParsers {
    def Type: Parser[TA] = (arrowType | iType | oType)
    def iType: Parser[TA] = "i" ^^ {x => Ti()}
    def oType: Parser[TA] = "o" ^^ {x => To()}
    def indexType: Parser[TA] = "e" ^^ {x => Tindex()}
    def arrowType: Parser[TA] = "("~> Type~"->"~Type <~")" ^^ {case in ~ "->" ~ out => ->(in,out)}
  }

  class TypeException(s: String) extends Exception
}
