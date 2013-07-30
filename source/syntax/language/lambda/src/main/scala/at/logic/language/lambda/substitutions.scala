package at.logic.language.lambda

import symbols._
import typedLambdaCalculus._
import BetaReduction._

package substitutions {

import collection.immutable.HashMap

/* substitution preserves the following:
* 1) it is a valid function, i.e. order of elements is irrelevant and each variable is mapped to only one element
* 2) all mappings are applied simultaneously to a term i.e. {x |-> y, y |-> a}x = y and not a.
*/
  class Substitution[T <: LambdaExpression] protected[substitutions](val map: Map[Var, T]) extends (T => T) {
    def ::(sub:Tuple2[Var, T]) = new Substitution(map + sub)
    def :::(otherSubstitution:Substitution[T]) = new Substitution(map ++ otherSubstitution.map.iterator)
    def apply(expression: T): T = applyWithChangeDBIndices(expression, Nil)
    def applyAndBetaNormalize(expr : T) : T = betaNormalize(apply(expr))(StrategyOuterInner.Outermost).asInstanceOf[T] //TODO:instead of casting, change betanormalize

    override def equals(a: Any) = a match {
      case s: Substitution[_] => map.equals(s.map)
      case _ => false
    }

    //an identity function maps all terms to themselves
    def isIdentity = map.filterNot((p : (Var,T)) => p._1 == p._2).isEmpty

    override def hashCode() = map.hashCode
    override def toString = map.toString

    // the change of db indices is done automatically in the constructor of abs
    // NOTE: the list protectedVars contains the bound variables of the whole expression
    protected def applyWithChangeDBIndices(expression: T, protectedVars: List[Var]): T = {
      // compute the set of free variables of the range of the substitution in order to avoid variables capture
      val freeVarsInRange = map.foldLeft(Set[Var]().empty){case (set,(x,t)) => set ++ t.freeVariables}
      applyWithChangeDBIndices(expression, protectedVars, freeVarsInRange)
    }
    protected def applyWithChangeDBIndices(expression: T, protectedVars: List[Var], freeVarsInRange: Set[Var]): T = expression match {
      case x:Var if !(protectedVars.contains(x)) => {
        map.get(x) match {
          case Some(t) => {
            val freevarsWithDBIndex = checkLambdaExpression(t)
            if (!freevarsWithDBIndex.isEmpty) {
              //println("ERROR: bound variables "+ freevarsWithDBIndex +" outside of binding context in term "+t.toStringSimple)
              throw new Exception("ERROR: bound variables "+ freevarsWithDBIndex +" outside of binding context in term "+t.toStringSimple)
            }
            t
          }
          case None => {
            x.asInstanceOf[T]
          }
        }
      }
      case x:Var => {
        //if (map.contains( x ) )
          // this can happen as the proofs may not be regular and in general we would like to ignore in this case
          //println("WARNING: trying to substitute for a bound variable, ignoring!")
       expression
      }
      case App(m,n) => App(applyWithChangeDBIndices(m.asInstanceOf[T], protectedVars,freeVarsInRange),
        applyWithChangeDBIndices(n.asInstanceOf[T], protectedVars, freeVarsInRange)).asInstanceOf[T]
      case abs: Abs if freeVarsInRange.contains(abs.variable) => {
        val newbvar = abs.variable.factory.createVar(FreshVariableSymbolFactory.getVariableSymbol,abs.variable.exptype)
        val tsub = Substitution[T]((abs.variable,newbvar.asInstanceOf[T]))
        Abs(newbvar, applyWithChangeDBIndices(tsub(abs.expression.asInstanceOf[T]).asInstanceOf[T],newbvar::protectedVars, freeVarsInRange)).asInstanceOf[T]
      }
      case abs: Abs => Abs(abs.variable, applyWithChangeDBIndices(abs.expression.asInstanceOf[T],abs.variable::protectedVars,
        freeVarsInRange)).asInstanceOf[T]
      case _ => expression
    }

    // make sure the overriden keys are of the applying sub
    // note: compose is in function application notation i.e. (sigma compose tau) apply x = sigma(tau(x)) = x.tau.sigma
    def compose(sub: Substitution[T]): Substitution[T] = Substitution(map ++ sub.map.map(x => (x._1, apply(x._2))))

    // like compose but do not apply the first sub to the second not that the sub might not be idempotent
    def simultaneousCompose(sub: Substitution[T]): Substitution[T] = Substitution(map ++ sub.map)

    def isRenaming = map.forall( p => p._2.isInstanceOf[Var] )

    def getTerm(v: Var) = map.get(v) match {
      case Some(t) => t
      case None => throw new Exception("ERROR: No term associated with variable " + v + " in substitution " + this)
    }

  }

  object Substitution {
    def apply[T <: LambdaExpression](subs: Iterator[Tuple2[Var, T]]): Substitution[T] = new Substitution(new HashMap[Var, T]() ++ subs)
    def apply[T <: LambdaExpression](subs: Tuple2[Var, T]*): Substitution[T] = apply(subs.iterator)
    def apply[T <: LambdaExpression](subs: List[Tuple2[Var, T]]): Substitution[T] = apply(subs.iterator)
    def apply[T <: LambdaExpression](variable: Var, expression: T): Substitution[T] = apply((variable, expression))
    def apply[T <: LambdaExpression](map: Map[Var, T]): Substitution[T] = new Substitution( map )
    def apply() = new Substitution(new HashMap())
  }

  object ImplicitConverters {
    implicit def convertSubstitutionToPair[T <: LambdaExpression](sub: Substitution[T]): Tuple2[Var,T] = {
      require(sub.map.size == 1)
      sub.map.iterator.next
    }
    implicit def convertPairToSubstitution[T <: LambdaExpression](pair: Tuple2[Var, T]): Substitution[T] = Substitution(pair)
  }
}
