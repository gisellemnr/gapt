package at.logic.algorithms.resolution

import at.logic.calculi.lk.base.{FSequent, LKProof}
import at.logic.calculi.lk.propositionalRules._
import at.logic.calculi.resolution.base.FClause
import at.logic.language.hol._
import at.logic.calculi.lk.base.types.FSequent
import at.logic.calculi.lk.quantificationRules.{ExistsRightRule, ForallLeftRule}
import at.logic.calculi.lk.base.types.FSequent
  import at.logic.language.lambda.substitutions.Substitution
import scala.Some
import scala.Tuple2

/**
 * Given a formula f and a clause a in CNF(-f), PCNF computes a proof of s o a (see logic.at/ceres for the definition of o)
 */
object PCNF {
  /**
   * @param s a sequent not containing strong quantifiers
   * @param a a clause
   * @return an LK proof of s o a (see logic.at/ceres for the definition of o)
   */
  def apply(s: FSequent, a: FClause): LKProof = {
    // compute formula
    val form = if (!s.antecedent.isEmpty)
      s.succedent.foldLeft(s.antecedent.reduceLeft((f1,f2) => And(f1,f2)))((f1,f2) => And(f1,Neg(f2)))
    else
      s.succedent.tail.foldLeft(Neg(s.succedent.head))((f1,f2) => And(f1,Neg(f2)))

    // compute CNF and confirm a <- CNF(-s) up to variable renaming
    val cnf = CNFp(form)
    var sub = Substitution[HOLExpression]()
    val op = cnf.find(y => getVariableRenaming(y,a) match {
      case Some(s) => {sub = s; true}
      case _ => false
    })
    val (p,f,inAntecedent) = op match {
      case Some(f2) =>
        // find the right formula and compute the proof
        s.antecedent.find(x => CNFp(x).contains(f2)) match {
          case Some(f) => (PCNFp(sub(f).asInstanceOf[HOLFormula],a),f,true)
          case _ => {
            val f = s.succedent.find(x => CNFn(x).contains(f2)).get
            (PCNFn(sub(f).asInstanceOf[HOLFormula],a),f,false)
          }
        }
      case None =>
        // check for reflexivity
        a.pos.find(f => f match {
          case Equation(a,b) if a == b => true
          case at.logic.language.fol.Equation(a,b) if a == b => true // TOFIX: remove when bug 224 is solved
          case  _ => false
        }) match {
          case Some(f) => (Axiom(List(),List(f)),f.asInstanceOf[HOLFormula],false)
          case _ => throw new IllegalArgumentException("Clause [" + a.toString + "] is not reflexivity and not contained in CNF(-s) [\n" + cnf.mkString(";\n") + "\n]")
        }
    }
    // apply weakenings
    (if (!inAntecedent) removeFirst(s.succedent,f) else s.succedent).foldLeft(
      (if (inAntecedent) (removeFirst(s.antecedent,f)) else s.antecedent).foldLeft(p)((pr,f)
        => WeakeningLeftRule(pr,sub(f).asInstanceOf[HOLFormula]))
    )((pr,f) => WeakeningRightRule(pr,sub(f).asInstanceOf[HOLFormula]))
  }

  /**
   * assuming a in CNF^-(f) we give a proof of a o |- f
   * @param f
   * @param a
   * @return
   */
  private def PCNFn(f: HOLFormula, a: FClause): LKProof = f match {
    case Atom(_,_) => Axiom(List(f),List(f))
    case Neg(f2) => NegRightRule(PCNFp(f2,a), f2)
    case And(f1,f2) => {
      /* see Or in PCNFp
      // get all possible partitions of the ant and suc of the clause a
      val prod = for ((c1,c2) <- power(a.neg.toList); (d1,d2) <- power(a.pos.toList)) yield (FClause(c1,d1),FClause(c2,d2))
      // find the right partition
      val cnf1 = CNFn(f1)
      val cnf2 = CNFn(f2)
      val par = prod.find(x => cnf1.contains(x._1) && cnf2.contains(x._2)).get
      // create the proof
      AndRightRule(PCNFn(f1,par._1), PCNFn(f2,par._2), f1, f2)
      */
      AndRightRule(PCNFn(f1,a), PCNFn(f2,a), f1, f2)
    }
    case Or(f1,f2) =>
      if (CNFn(f1).contains(a)) OrRight1Rule(PCNFn(f1,a),f1,f2)
      else OrRight2Rule(PCNFn(f2,a),f1,f2)
    case Imp(f1,f2) =>
      if (CNFp(f1).contains(a)) ImpRightRule(WeakeningRightRule(PCNFp(f1,a), f2), f1,f2)
      else ImpRightRule(WeakeningLeftRule(PCNFn(f2,a), f1), f1, f2)
    case ExVar(v,f2) => ExistsRightRule(PCNFn(f2, a), f2 ,f, v.asInstanceOf[HOLVar])
    case _ => throw new IllegalArgumentException("unknown head of formula: " + a.toString)
  }

  /**
   * assuming a in CNF^+(f) we give a proof of a o f |-
   * @param f
   * @param a
   * @return
   */
  private def PCNFp(f: HOLFormula, a: FClause): LKProof = f match {
    case Atom(_,_) => Axiom(List(f),List(f))
    case Neg(f2) => NegLeftRule(PCNFn(f2,a), f2)
    case And(f1,f2) =>
      if (CNFp(f1).contains(a)) AndLeft1Rule(PCNFp(f1,a),f1,f2)
      else AndLeft2Rule(PCNFp(f2,a),f1,f2)
    case Or(f1,f2) => {
      /* the following is an inefficient way to compute the exact context sequents
      // get all possible partitions of the ant and suc of the clause a
      val prod = for ((c1,c2) <- power(a.neg.toList); (d1,d2) <- power(a.pos.toList)) yield (FClause(c1,d1),FClause(c2,d2))
      // find the right partition
      val cnf1 = CNFp(f1)
      val cnf2 = CNFp(f2)
      val par = prod.find(x => cnf1.contains(x._1) && cnf2.contains(x._2)).get
      // create the proof
      OrLeftRule(PCNFp(f1,par._1), PCNFp(f2,par._2), f1, f2)
      we just take the whole context and apply weakenings later */
      OrLeftRule(PCNFp(f1,a), PCNFp(f2,a), f1, f2)
    }
    case Imp(f1,f2) => {
      // get all possible partitions of the ant and suc of the clause a
      val prod = for ((c1,c2) <- power(a.neg.toList); (d1,d2) <- power(a.pos.toList)) yield (FClause(c1,d1),FClause(c2,d2))
      // find the right partition
      val cnf1 = CNFn(f1)
      val cnf2 = CNFp(f2)
      val par = prod.find(x => cnf1.contains(x._1) && cnf2.contains(x._2)).get
      // create the proof
      ImpLeftRule(PCNFn(f1,par._1), PCNFp(f2,par._2), f1, f2)
    }
    case AllVar(v,f2) => ForallLeftRule(PCNFp(f2, a), f2, f, v.asInstanceOf[HOLVar])
    case _ => throw new IllegalArgumentException("unknown head of formula: " + a.toString)
  }

  def getVariableRenaming(f1: FClause, f2: FClause): Option[Substitution[HOLExpression]] = {
    if (f1.neg.size != f2.neg.size || f1.pos.size != f2.pos.size) None
    else {
      val pairs = (f1.neg.asInstanceOf[Seq[HOLExpression]].zip(f2.neg.asInstanceOf[Seq[HOLExpression]])
        ++ f1.pos.asInstanceOf[Seq[HOLExpression]].zip(f2.pos.asInstanceOf[Seq[HOLExpression]]))
      try {
        val sub = pairs.foldLeft(Substitution[HOLExpression]())((sb,p) =>
            sb simultaneousCompose computeSub(p))
        if (pairs.forall(p => sub(p._1) == p._2)) Some(sub) else None
      } catch {
        case e: Exception => None
      }
    }
  }
  def computeSub(p: Pair[HOLExpression,HOLExpression]): Substitution[HOLExpression] = (p._1, p._2) match {
    case (HOLVar(a,_), HOLVar(b,_)) if a == b =>Substitution[HOLExpression]()
    case (v1: HOLVar, v2: HOLVar) => Substitution(v1,v2)
    case (c1: HOLConst, c2: HOLConst) => Substitution[HOLExpression]()
    case (HOLApp(a1,b1),HOLApp(a2,b2)) =>
      computeSub((a1,a2)) simultaneousCompose computeSub(b1,b2)
    case (HOLAbs(v1,a1), HOLAbs(v2,a2)) => Substitution(computeSub(a1,a2).map - v1)
    case _ => throw new Exception()
  }

  // we need to compute (Not anymore)) the power set of the literals of the clause in order to find the right division of them in and right and or left
  def power[A](lst: List[A]): List[Tuple2[List[A],List[A]]] = {
    @annotation.tailrec
    def pwr(s: List[A], acc: List[Tuple2[List[A],List[A]]]): List[Tuple2[List[A],List[A]]] = s match {
      case Nil     => acc
      case a :: as => pwr(as, acc ::: (acc map (x => (a :: x._1,removeFirst(x._2,a).toList))))
    }
    pwr(lst, (Nil,lst) :: Nil)
  }

  def removeFirst[A](s: Seq[A], a: A): Seq[A] = {
    val index = s.indexOf(a)
    s.take(index) ++ s.takeRight(s.size-index-1)
  }

}

