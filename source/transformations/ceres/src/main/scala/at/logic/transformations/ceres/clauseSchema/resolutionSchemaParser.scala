package at.logic.transformations.ceres.clauseSchema

import at.logic.calculi.lk.macroRules._
import at.logic.calculi.slk._
import at.logic.calculi.lk.base.{Sequent, LKProof}
import at.logic.calculi.lk.propositionalRules._
import scala.util.parsing.combinator._
import scala.util.matching.Regex
import at.logic.language.hol._
import at.logic.language.hol.Definitions._
import at.logic.language.lambda.typedLambdaCalculus._
import at.logic.language.lambda.symbols.VariableStringSymbol
import collection.mutable.Map
import at.logic.language.lambda.types.Definitions._
import logicSymbols.{ConstantSymbolA, ConstantStringSymbol}
import java.io.InputStreamReader
import at.logic.calculi.lk.quantificationRules._
import at.logic.language.schema.{foVar, dbTRS, foTerm, indexedFOVar, sTerm, SchemaFormula, BigAnd, BigOr, IntVar, IntegerTerm, IndexedPredicate, Succ, IntZero, Neg => SNeg}

object ParseResSchema {

  def apply(txt: InputStreamReader): Unit = {
    var map  = Map.empty[String, LKProof]
    var baseORstep: Int = 1
    SchemaProofDB.clear
    var defMap = Map.empty[HOLConst, Tuple2[List[IntegerTerm] ,SchemaFormula]]
    //    lazy val sp2 = new ParserTxt
    //    sp2.parseAll(sp2.line, txt)
    val mapPredicateToArity = Map.empty[String, Int]
    dbTRS.clear
    dbTRSresolutionSchema.clear
    lazy val sp = new SimpleResolutionSchemaParser

    sp.parseAll(sp.resSchema, txt) match {
      case sp.Success(result, input) => // println("\n\nSUCCESS parse :) \n")
      case x: AnyRef => // { println("\n\nFAIL parse : \n"+error_buffer); throw new Exception("\n\nFAIL parse :( \n"); }
        throw new Exception(x.toString)
    }


    class SimpleResolutionSchemaParser extends JavaTokenParsers with at.logic.language.lambda.types.Parsers {

      def name = """[\\]*[a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z,_,0,1,2,3,4,5,6,7,8,9]*""".r

      def resSchema: Parser[Unit] =  rep(trs) ~ rep(resTRS) ^^ {
        case rw_trs ~ res_trs  => {
        }
      }

      //term-rewriting system for s-terms
      def trs: Parser[Unit] = s_term ~ "->" ~ term ~ s_term ~ "->" ~ term ^^ {
        case t1 ~ "->" ~ base ~ t2 ~ "->" ~ step => {
          t1 match {
            case sTerm(func1, i1, arg1) =>
              t2 match {
                case sTerm(func2, i2, arg2) => {
                  //                  if(func1 == func2) {
                  dbTRS.add(func1.asInstanceOf[HOLConst], base, step)
                  //                  }
                }
              }
          }
        }
      }

      //c(k+1, x1,...,X1,...)
      def c_term: Parser[clauseSchema] = "c" ~ "(" ~ index ~ "," ~ fo2var ~ "," ~ sclause_var ~ ")" ^^ {
        case "c" ~ "(" ~ ind ~ "," ~ fo ~ "," ~ sclvar ~ ")" => {
          clauseSchema("c", ind::fo::sclvar::Nil)
        }
      }

      //clause schema inductive definition: clauses, X, c(k+1, x1,...,X1,...), composition
      def clause_schema: Parser[sClause] = c_term | sclause_var | non_varSclause | composition

      // a usual clause
      def non_varSclause: Parser[sClause] = rep(atom) ~ "|-" ~ rep(atom) ^^ {
        case ant ~ "|-" ~ succ => nonVarSclause(ant, succ)
      }

      //composition of clauses
      def composition: Parser[sClause] = "(" ~ clause_schema ~ "o" ~ clause_schema ~ ")" ^^ {
        case "(" ~ c1 ~ "o" ~ c2  ~ ")" => sClauseComposition(c1, c2)
      }

      // clause variable
      def sclause_var: Parser[sClause] = "[X,Y]" ^^ {
        case str => sClauseVar(str)
      }

      //resolution term inductive definition
      def res_term: Parser[sResolutionTerm] = r_term | clause_schema

      //rTerm
      def r_term: Parser[sResolutionTerm] = "r" ~ "(" ~ rho_term ~ ";" ~ non_varSclause ~ ";" ~ atom ~ ")" ^^ {
        case "r" ~ "(" ~ rho_t ~ ";" ~ non_varScl ~ ";" ~ at ~ ")" => rTerm(rho_t, non_varScl, at)
      }

      // TODO: X is missing
      def rho_term: Parser[sResolutionTerm] = """[ρ][0,1,2,3]*""".r ~ "(" ~ index ~ "," ~ fo2var ~ ")" ^^ {
        case str ~ "(" ~ ind ~ "," ~ fo2v ~ ")" =>
          ResolutionProofSchema(str, ind::fo2v::Nil)
      }

      def fo2var: Parser[HOLVar] = "z" ^^ {
        case str => fo2Var(new VariableStringSymbol(str))
      }

      def r_term_OR_clause: Parser[sResolutionTerm] = non_varSclause | r_term

      //term-rewriting system for r-terms
      def resTRS: Parser[Unit] = rho_term ~ "->" ~ r_term_OR_clause ~ rho_term ~ "->" ~ r_term ^^ {
        case rho1 ~ "->" ~ base ~ rho2 ~ "->" ~ step => {
          rho1 match {
            case ResolutionProofSchema(name, arg1) =>
              rho2 match {
                case ResolutionProofSchema(name1, arg2) => {
                  //                  if(name == name1) {
                  dbTRSresolutionSchema.add(name, Tuple2(rho1, base), Tuple2(rho2, step))
                  println(dbTRSresolutionSchema.map)
                  //                  }
                }
              }
          }
        }
      }

      def label: String = """[0-9]*[root]*"""

      def term: Parser[HOLExpression] = ( non_formula | formula)
      def formula: Parser[HOLFormula] = (atom | neg | big | and | or | indPred | imp | forall | exists | variable | constant) ^? {case trm: Formula => trm.asInstanceOf[HOLFormula]}
      def intTerm: Parser[HOLExpression] = index //| schemaFormula
      def index: Parser[IntegerTerm] = (sum | intConst | intVar | succ  )
      def intConst: Parser[IntegerTerm] = (intZero | intOne | intTwo | intThree)
      def intOne :  Parser[IntegerTerm] = "1".r ^^ { case x => {  Succ(IntZero())}}
      def intTwo :  Parser[IntegerTerm] = "2".r ^^ { case x => {  Succ(Succ(IntZero()))}}
      def intThree: Parser[IntegerTerm] = "3".r ^^ { case x => {  Succ(Succ(Succ(IntZero())))}}
      def intZero:  Parser[IntegerTerm] = "0".r ^^ { case x => {  IntZero()}
      }

      def sum : Parser[IntegerTerm] = intVar ~ "+" ~ intConst ^^ {case indV ~ "+" ~ indC => {
        //        println("\n\nsum")
        indC match {
          case Succ(IntZero()) => Succ(indV)
          case Succ(Succ(IntZero())) => Succ(Succ(indV))
          case Succ(Succ(Succ(IntZero()))) => Succ(Succ(Succ(indV)))
        }}}

      def intVar: Parser[IntVar] = "[i,j,m,n,k,x]".r ^^ {
        case x => { /*println("\n\nintVar");*/ IntVar(new VariableStringSymbol(x))}
      }
      def succ: Parser[IntegerTerm] = "s(" ~ intTerm ~ ")" ^^ {
        case "s(" ~ intTerm ~ ")" => Succ(intTerm.asInstanceOf[IntegerTerm])
      }

      def schemaFormula = formula

      def indPred : Parser[HOLFormula] = """[A-Z]*[a-z]*[0-9]*""".r ~ "(" ~ repsep(index,",") ~ ")" ^^ {
        case x ~ "(" ~ l ~ ")" => {
          if (! mapPredicateToArity.isDefinedAt(x.toString) )
            mapPredicateToArity.put(x.toString, l.size)
          else if (mapPredicateToArity.get(x.toString).get != l.size ) {
            println("\nInput ERROR : Indexed Predicate '"+x.toString+"' should have arity "+mapPredicateToArity.get(x.toString).get+ ", but not "+l.size+" !\n\n")
            throw new Exception("\nInput ERROR : Indexed Predicate '"+x.toString+"' should have arity "+mapPredicateToArity.get(x.toString).get+ ", but not "+l.size+" !\n")
          }
          //          println("\n\nIndexedPredicate");

          //          val map: scala.collection.immutable.Map[Var, T])
          //          val subst: SchemaSubstitution1[HOLExpression] = new SchemaSubstitution1[HOLExpression]()
          //          val new_ind = subst(ind)
          //          val new_map = (subst.map - subst.map.head._1.asInstanceOf[Var]) + Pair(subst.map.head._1.asInstanceOf[Var], Pred(new_ind.asInstanceOf[IntegerTerm]) )
          //          val new_subst = new SchemaSubstitution1(new_map)

          IndexedPredicate(new ConstantStringSymbol(x), l)
        }
      }


      def define: Parser[Unit]  = indPred ~ ":=" ~ schemaFormula ^^ {
        case indpred ~ ":=" ~ sf => {
          indpred match {
            case IndexedPredicate(f,ls) => {
              defMap.put(f, Tuple2(ls.asInstanceOf[List[IntegerTerm]],sf.asInstanceOf[SchemaFormula]))
            }
          }
        }
      }


      // nested bigAnd bigOr....           ("""BigAnd""".r | """BigOr""".r)
      def prefix : Parser[Tuple4[Boolean, IntVar, IntegerTerm, IntegerTerm]] = """[BigAnd]*[BigOr]*""".r ~ "(" ~ intVar ~ "=" ~ index ~ ".." ~ index ~ ")" ^^ {
        case "BigAnd" ~ "(" ~ intVar1 ~ "=" ~ ind1 ~ ".." ~ ind2 ~ ")"  => {
          //          println("\n\nprefix\n\n")
          Tuple4(true, intVar1, ind1, ind2)
        }
        case "BigOr" ~ "(" ~ intVar1 ~ "=" ~ ind1 ~ ".." ~ ind2 ~ ")"  => {
          //          println("\n\nprefix\n\n")
          Tuple4(false, intVar1, ind1, ind2)
        }
      }

      def big : Parser[HOLFormula] = rep1(prefix) ~ schemaFormula ^^ {
        case l ~ schemaFormula  => {
          //          println("Works?")
          l.reverse.foldLeft(schemaFormula.asInstanceOf[SchemaFormula])((res, triple) => {
            if (triple._1)
              BigAnd(triple._2, res, triple._3, triple._4)
            else
              BigOr(triple._2, res, triple._3, triple._4)
          })
        }
      }
      def non_formula: Parser[HOLExpression] = (fo_term | s_term | indexedVar | abs | variable | constant | var_func | const_func)
      def s_term: Parser[HOLExpression] = "[g,h]".r ~ "(" ~ intTerm ~ "," ~ non_formula ~ ")" ^^ {
        case name ~ "(" ~ i ~ "," ~ args ~ ")" => {
          //          println("\n\nsTerm\n)")
          //          println("args = "+args)
          //          println("args.extype = "+args.exptype)
          sTerm(name, i.asInstanceOf[IntegerTerm], args::Nil)
        }
      }
      def fo_term: Parser[HOLExpression] = "[f]".r ~ "(" ~ non_formula ~ ")" ^^ {
        case name ~ "(" ~ arg ~ ")" => {
          //          println("\n\nfoTerm\n arg.extype = "+arg.exptype)
          foTerm(name, arg::Nil)
        }
      }
      def indexedVar: Parser[HOLVar] = regex(new Regex("[z]")) ~ "(" ~ intTerm ~ ")" ^^ {
        case x ~ "(" ~ index ~ ")" => {
          indexedFOVar(new VariableStringSymbol(x), index.asInstanceOf[IntegerTerm])
        }
      }

      // TODO: a should be a FOConstant
      def FOVariable: Parser[HOLVar] = regex(new Regex("[x,y,a]" + word))  ^^ {case x => foVar(x)}
      def variable: Parser[HOLVar] = (indexedVar | FOVariable)//regex(new Regex("[u-z]" + word))  ^^ {case x => hol.createVar(new VariableStringSymbol(x), i->i).asInstanceOf[HOLVar]}
      def constant: Parser[HOLConst] = regex(new Regex("[a-tA-Z0-9]" + word))  ^^ {case x => hol.createVar(new ConstantStringSymbol(x), ind->ind).asInstanceOf[HOLConst]}
      def and: Parser[HOLFormula] = "(" ~ repsep(formula, "/\\") ~ ")" ^^ { case "(" ~ formulas ~ ")"  => { formulas.tail.foldLeft(formulas.head)((f,res) => And(f, res)) } }
      def or: Parser[HOLFormula]  = "(" ~ repsep(formula, """\/""" ) ~ ")" ^^ { case "(" ~ formulas ~ ")"  => { formulas.tail.foldLeft(formulas.head)((f,res) => Or(f, res)) } }
      def imp: Parser[HOLFormula] = "Imp" ~ formula ~ formula ^^ {case "Imp" ~ x ~ y => Imp(x,y)}
      def abs: Parser[HOLExpression] = "Abs" ~ variable ~ term ^^ {case "Abs" ~ v ~ x => Abs(v,x).asInstanceOf[HOLExpression]}
      def neg: Parser[HOLFormula] = "~" ~ formula ^^ {case "~" ~ x => Neg(x)}
      def atom: Parser[HOLFormula] = (equality | var_atom | const_atom)
      def forall: Parser[HOLFormula] = "Forall" ~ variable ~ formula ^^ {case "Forall" ~ v ~ x => AllVar(v,x)}
      def exists: Parser[HOLFormula] = "Exists" ~ variable ~ formula ^^ {case "Exists" ~ v ~ x => ExVar(v,x)}
      def var_atom: Parser[HOLFormula] = regex(new Regex("[u-z]" + word)) ~ "(" ~ repsep(term,",") ~ ")" ^^ {case x ~ "(" ~ params ~ ")" => {
        //        println("\n\nvar_atom")
        Atom(new VariableStringSymbol(x), params)
      }}

      //      def const_atom: Parser[HOLFormula] = regex(new Regex("["+symbols+"a-tA-Z0-9]" + word)) ~ "(" ~ repsep(term,",") ~ ")" ^^ {case x ~ "(" ~ params ~ ")" => {
      def const_atom: Parser[HOLFormula] = regex(new Regex("P")) ~ "(" ~ repsep(term,",") ~ ")" ^^ {case x ~ "(" ~ params ~ ")" => {

        //        println("\n\nconst_atom")
        Atom(new ConstantStringSymbol(x), params)
      }}
      def equality: Parser[HOLFormula] = /*eq_infix | */ eq_prefix // infix is problematic in higher order
      //def eq_infix: Parser[HOLFormula] = term ~ "=" ~ term ^^ {case x ~ "=" ~ y => Equation(x,y)}
      def eq_prefix: Parser[HOLFormula] = "=" ~ "(" ~ term ~ "," ~ term  ~ ")" ^^ {case "=" ~ "(" ~ x ~ "," ~ y  ~ ")" => Equation(x,y)}
      def var_func: Parser[HOLExpression] = regex(new Regex("[u-z]" + word)) ~ "(" ~ repsep(term,",") ~ ")" ^^ {case x ~ "(" ~ params ~ ")"  => Function(new VariableStringSymbol(x), params, ind->ind)}
      /*def var_func: Parser[HOLExpression] = (var_func1 | var_funcn)
      def var_func1: Parser[HOLExpression] = regex(new Regex("[u-z]" + word)) ~ "(" ~ repsep(term,",") ~ ")"  ~ ":" ~ Type ^^ {case x ~ "(" ~ params ~ ")" ~ ":" ~ tp => Function(new VariableStringSymbol(x), params, tp)}
      def var_funcn: Parser[HOLExpression] = regex(new Regex("[u-z]" + word)) ~ "^" ~ decimalNumber ~ "(" ~ repsep(term,",") ~ ")"  ~ ":" ~ Type ^^ {case x ~ "^" ~ n ~ "(" ~ params ~ ")" ~ ":" ~ tp => genF(n.toInt, HOLVar(new VariableStringSymbol(x)), params)}
      */
      def const_func: Parser[HOLExpression] = regex(new Regex("["+symbols+"a-tA-Z0-9]" + word)) ~ "(" ~ repsep(term,",") ~ ")"  ^^ {case x ~ "(" ~ params ~ ")"  => Function(new ConstantStringSymbol(x), params, ind->ind)}
      protected def word: String = """[a-zA-Z0-9$_{}]*"""
      protected def symbol: Parser[String] = symbols.r
      def symbols: String = """[\053\055\052\057\0134\0136\074\076\075\0140\0176\077\0100\046\0174\041\043\047\073\0173\0175]+""" // +-*/\^<>=`~?@&|!#{}';


      def proof_name : Parser[String] = """[\\]*[a-z]*[0-9]*""".r


      def termDefL1: Parser[LKProof] = "termDefL1(" ~ label.r ~ "," ~ formula ~ "," ~ formula ~ ")" ^^ {
        case "termDefL1(" ~ l ~ "," ~ f1 ~ "," ~ f2 ~ ")" => {
          TermLeftEquivalenceRule1(map.get(l).get, f1.asInstanceOf[HOLFormula], f2.asInstanceOf[HOLFormula])
        }
      }

      def termDefR1: Parser[LKProof] = "termDefR1(" ~ label.r ~ "," ~ formula ~ "," ~ formula ~ ")" ^^ {
        case "termDefR1(" ~ l ~ "," ~ f1 ~ "," ~ f2 ~ ")" => {
          TermRightEquivalenceRule1(map.get(l).get, f1.asInstanceOf[HOLFormula], f2.asInstanceOf[HOLFormula])
        }
      }
    }
    //    println("\n\nnumber of SLK-proofs = "+bigMap.size)
    //    println("\ndefMapr size = "+defMap.size)

    //    println("\n\n\nlist = "+list)
    //    if (!bigMap.get("chi").get._2.isDefinedAt(plabel)) println("\n\n\nSyntax ERROR after ID : " + error_buffer +"\n\n")
    //    val m = bigMap.get("chi").get._2.get(plabel).get
    ////    println(m.root.antecedent.head+" |- "+m.root.succedent.head)
    //    m
    //  println("\nSchemaProofDB.size = "+SchemaProofDB.size+"\n")
//    bigMap
  }
}
