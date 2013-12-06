/*
 * FOLerization.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package at.logic.algorithms.fol

import at.logic.language.fol._
import at.logic.language.hol.{HOLExpression, HOLVar, HOLConst, Neg => HOLNeg, And => HOLAnd, Or => HOLOr, Imp => HOLImp, Function => HOLFunction, Atom => HOLAtom, HOLFormula}
import at.logic.language.hol.{ExVar => HOLExVar, AllVar => HOLAllVar}
import at.logic.language.hol.logicSymbols._
import at.logic.language.lambda.typedLambdaCalculus._
import at.logic.language.lambda.types._
import at.logic.language.lambda.symbols._
import scala.collection.mutable
import at.logic.calculi.lk.base.types._
import at.logic.language.hol.{HOLApp, HOLAbs, Function => HOLFunction}
import at.logic.language.lambda.symbols.VariableStringSymbol
import at.logic.language.hol.logicSymbols.ConstantStringSymbol
import at.logic.language.schema.{IntZero,Succ,foVar, foConst,IntegerTerm,indexedFOVar}

package hol2fol {

import at.logic.language.hol.HOLOrdering
import at.logic.calculi.lk.base.FSequent
import at.logic.calculi.lk.base.types.FSequent


/**
 * Try to reduce high order terms to first order terms by changing the types if possible. Closed lambda expression are
 * replaced by constants. Open lambda expressions are changed by functions.
 */
object reduceHolToFol extends reduceHolToFol
class reduceHolToFol {
  //TODO: replace mutable maps by immutable ones to allow parallelism. Need to check the calls for sideffects on the maps
  def apply(formula: HOLFormula, scope: mutable.Map[LambdaExpression, ConstantStringSymbol], id: {def nextId: Int}): FOLFormula = {
    val immscope = Map[LambdaExpression, ConstantStringSymbol]() ++ scope
    val (scope_, qterm) = replaceAbstractions(formula, immscope, id)
    scope ++= scope_
    apply_( qterm).asInstanceOf[FOLFormula]
  }

  // convienience method creating empty scope and default id
  def apply(term: HOLExpression) : FOLExpression = {
    val counter = new {private var state = 0; def nextId = { state = state +1; state}}
    val emptymap = mutable.Map[LambdaExpression, ConstantStringSymbol]()
    reduceHolToFol( term, emptymap, counter )
  }

  def apply(formula: HOLFormula) : FOLFormula =
  //inner cast needed to call the correct apply method
    reduceHolToFol(formula.asInstanceOf[HOLExpression]).asInstanceOf[FOLFormula]

  def apply(term: HOLExpression, scope: mutable.Map[LambdaExpression, ConstantStringSymbol], id: {def nextId: Int}) = {
    val immscope = Map[LambdaExpression, ConstantStringSymbol]() ++ scope
    val (scope_, qterm) = replaceAbstractions(term, immscope, id)
    scope ++= scope_
    apply_( qterm)
  }

  def apply(s: FSequent, scope: mutable.Map[LambdaExpression, ConstantStringSymbol], id: {def nextId: Int}): FSequent = {
    val immscope = Map[LambdaExpression,ConstantStringSymbol]() ++ scope
    val (scope1, ant) = s.antecedent.foldLeft((immscope, List[HOLFormula]()))((r, formula) => {
      val (scope_, f_) = replaceAbstractions(formula, r._1, id)
      (scope_, f_.asInstanceOf[HOLFormula] :: r._2)
    })
    val (scope2, succ) = s.succedent.foldLeft((scope1, List[HOLFormula]()))((r, formula) => {
      val (scope_, f_) = replaceAbstractions(formula, r._1, id)
      (scope_, f_.asInstanceOf[HOLFormula] :: r._2)
    })
    scope ++= scope2
    FSequent( ant map apply_, succ map apply_  )
  }

  def apply_(f:HOLFormula) : FOLFormula =
    apply_(f.asInstanceOf[HOLExpression]).asInstanceOf[FOLFormula]
  def apply_(term: HOLExpression): FOLExpression = {
    term match {
      case e : FOLExpression => e // if it's already FOL - great, we are done.
      case z:indexedFOVar => FOLVar(new VariableStringSymbol(z.name.toString ++ intTermLength(z.index.asInstanceOf[IntegerTerm]).toString))
      case fov: foVar => FOLVar(new VariableStringSymbol(fov.name.toString))
      case foc: foConst => FOLConst(new ConstantStringSymbol(foc.name.toString))
      case HOLNeg(n) => Neg(reduceHolToFol(n))
      case HOLAnd(n1,n2) => And(reduceHolToFol(n1), reduceHolToFol(n2))
      case HOLOr(n1,n2) => Or(reduceHolToFol(n1), reduceHolToFol(n2))
      case HOLImp(n1,n2) => Imp(reduceHolToFol(n1), reduceHolToFol(n2))
      case HOLAllVar(v: HOLVar,n) => AllVar(reduceHolToFol(v).asInstanceOf[FOLVar], reduceHolToFol(n))
      case HOLExVar(v: HOLVar,n) => ExVar(reduceHolToFol(v).asInstanceOf[FOLVar], reduceHolToFol(n))
      case HOLAtom(n: ConstantSymbolA, ls) => Atom(n, ls.map(x => reduceHolToFol(x).asInstanceOf[FOLTerm]))
      case HOLFunction(n: ConstantSymbolA, ls, _) => Function(n, ls.map(x => reduceHolToFol(x).asInstanceOf[FOLTerm]))
      case HOLVar(n, _) => FOLVar(n)
      case HOLConst(n, _) => FOLConst(n)

      //this case is added for schema
      case HOLApp(func,arg) => {
        func match {
          case HOLVar(sym,_) => {
            val new_arg = apply_(arg).asInstanceOf[FOLTerm]
            return at.logic.language.fol.Function(new ConstantStringSymbol(sym.toString), new_arg::Nil)
          }
          case _ => println("\nWARNING: FO schema term!\n")
        }
        throw new Exception("\nProbably unrecognized object from schema!\n")
      }
      case _ => throw new IllegalArgumentException("Cannot reduce hol term: " + term.toString + " to fol as it is a higher order variable function or atom") // for cases of higher order atoms and functions
    }
  }


  //transforms a ground integer term to Int
  private def intTermLength(t: IntegerTerm): Int = t match {
    case c: IntZero => 0
    case Succ(t1) => 1 + intTermLength(t1)
    case _ => throw new Exception("\nError in reduceHolToFol.length(...) !\n")
  }
}


object replaceAbstractions extends replaceAbstractions
class replaceAbstractions {
  type ConstantsMap = Map[LambdaExpression,ConstantStringSymbol]

  def apply(l : List[FSequent]) : (ConstantsMap, List[FSequent]) = {
    val counter = new {private var state = 0; def nextId = { state = state +1; state}}
    val emptymap = Map[LambdaExpression, ConstantStringSymbol]()
    l.foldLeft((emptymap,List[FSequent]()))( (rec,el) => {
      val (scope_,f) = rec
      val (nscope, rfs) = replaceAbstractions(el, scope_,counter)
      (nscope, rfs::f)

    })
  }

  def apply(f:FSequent, scope : ConstantsMap, id: {def nextId: Int}) : (ConstantsMap,FSequent) = {
    val (scope1, ant) = f.antecedent.foldLeft((scope,List[HOLFormula]()))((rec,formula) => {
      val (scope_, f) = rec
      val (nscope, nformula) = replaceAbstractions(formula, scope_, id)
      (nscope, nformula.asInstanceOf[HOLFormula]::f)
    })
    val (scope2, succ) = f.succedent.foldLeft((scope1,List[HOLFormula]()))((rec,formula) => {
      val (scope_, f) = rec
      val (nscope, nformula) = replaceAbstractions(formula, scope_, id)
      (nscope, nformula.asInstanceOf[HOLFormula]::f)
    })

    (scope2, FSequent(ant.reverse, succ.reverse))
  }

  // scope and id are used to give the same names for new functions and constants between different calls of this method
  def apply(e : HOLExpression, scope : ConstantsMap, id: {def nextId: Int})
  : (ConstantsMap, HOLExpression) = e match {
    case HOLVar(_,_) =>
      (scope,e)
    case HOLConst(_,_) =>
      (scope,e)
    case HOLApp(s,t) =>
      val (scope1,s1) = replaceAbstractions(s, scope, id)
      val (scope2,t1) = replaceAbstractions(t, scope1, id)
      (scope2, HOLApp(s1,t1))
    //quantifiers should be kept
    case HOLAllVar(x,f) =>
      val (scope_, e_) = replaceAbstractions(f,scope,id)
      (scope_, HOLAllVar(x,e_.asInstanceOf[HOLFormula]))
    case HOLExVar(x,f) =>
      val (scope_, e_) = replaceAbstractions(f,scope,id)
      (scope_, HOLExVar(x,e_.asInstanceOf[HOLFormula]))
    // This case replaces an abstraction by a function term.
    // the scope we choose for the variant is the Abs itself as we want all abs identical up to variant use the same symbol
    case HOLAbs(v, exp) =>
      //systematically rename free variables for the index
      val normalizeda = e.variant(new VariantGenerator(new {var idd = 0; def nextId = {idd = idd+1; idd}}, "myVariantName"))
      //println("e: "+e)
      //println("norm: "+normalizeda)
      //update scope with a new constant if neccessary
      //println(scope)
      val scope_ = if (scope contains normalizeda) scope else scope + ((normalizeda,ConstantStringSymbol("q_{" + id.nextId + "}")))
      //println(scope_)
      val sym = scope_(normalizeda)

      val freeVarList = e.getFreeVariables.toList.sortBy(_.toString).asInstanceOf[List[HOLExpression]]
      if (freeVarList.isEmpty)
        (scope_, HOLConst( sym, e.exptype ))
      else
        (scope_, HOLFunction(sym, freeVarList, e.exptype))
    case _ =>
      throw new Exception("Unhandled case in abstraction replacement!"+e)

  }
}

/**
 * In contrast to reduceHolToFol, we recreate the term via the fol constructors but
 * do not try to remove higher-order content.
 */
object convertHolToFol extends convertHolToFol
class convertHolToFol {
  def apply(e:LambdaExpression) : FOLFormula = convertFormula(e)
  def apply(e:HOLFormula) : FOLFormula = convertFormula(e)
  def apply(fs:FSequent) : FSequent =
    FSequent(fs.antecedent.map(apply), fs.succedent.map(apply))

  def convertFormula(e:LambdaExpression) : FOLFormula = e match {
    case HOLAtom(sym:ConstantSymbolA, args)
      if (args.filterNot(_.exptype == Ti()).isEmpty) =>
      Atom(sym, args map convertTerm)

    case HOLNeg(x) => Neg(convertFormula(x))
    case HOLAnd(x,y) => And(convertFormula(x),convertFormula(y))
    case HOLOr(x,y) => Or(convertFormula(x),convertFormula(y))
    case HOLImp(x,y) => Imp(convertFormula(x),convertFormula(y))
    case HOLAllVar(x@Var(_, Ti()), t) => AllVar(convertTerm(x).asInstanceOf[FOLVar],convertFormula(t))
    case HOLExVar(x@Var(_, Ti()), t) => ExVar(convertTerm(x).asInstanceOf[FOLVar],convertFormula(t))
    case _ => throw new Exception("Could not convert term "+e+" to first order!")
  }

  def convertTerm(e:LambdaExpression) : FOLTerm = e match {
    case HOLVar(x:VariableSymbolA, Ti()) => FOLVar(x)
    case HOLConst(x:ConstantSymbolA, Ti()) => FOLConst(x)
    case HOLFunction(f:ConstantSymbolA, args, Ti())
      if (args.filterNot(_.exptype == Ti()).isEmpty) =>
      Function(f, args map convertTerm)
    case _ => throw new Exception("Could not convert term "+e+" to first order!")
  }

}

// TODO - support generated function symbols by checking the arity from le and add the variables to the returned term. Right now only constants are supported
object createExampleFOLConstant {
  def apply(le: LambdaExpression, css: ConstantStringSymbol) = FOLConst(css)
}


}
