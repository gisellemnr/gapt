/*
 * GAPScalaInteractiveShellLibrary.scala
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package at.logic.cli

import scala.tools.nsc.MainGenericRunner
import at.logic.cli.GAPScalaInteractiveShellLibrary._
import at.logic.language.fol._
import java.io._

object CLIMain {

  val script = """  
  import at.logic.cli.GAPScalaInteractiveShellLibrary._
  import at.logic.language.lambda.types._
  import at.logic.language.lambda.typedLambdaCalculus._
  import at.logic.language.hol._
  import at.logic.language.fol._
  import at.logic.calculi.lk._
  import at.logic.calculi.lk.base._
  import at.logic.calculi.lk.propositionalRules._
  import at.logic.calculi.lk.quantificationRules._
  import at.logic.calculi.lk.definitionRules._
  import at.logic.calculi.lk.equationalRules._
  import at.logic.calculi.lk.macroRules._
  import at.logic.calculi.lk.base.types.FSequent
  import at.logic.calculi.lk.base.FSequent
  import at.logic.calculi.lksk.base._
  import at.logic.language.lambda.symbols._
  import at.logic.language.hol.logicSymbols._
  import at.logic.transformations.skolemization.skolemize
  import at.logic.algorithms.lk.regularize
  import at.logic.calculi.occurrences.FormulaOccurrence
  import help.{apply => help}
  import at.logic.cli.GPL.{apply => copying, printLicense => license}
  import at.logic.algorithms.cutIntroduction.FlatTermSet
  import at.logic.algorithms.lk.statistics._

  println()
  println("    *************************************")
  println("    *    Welcome to the GAPT shell!     *")
  println("    *  See help for a list of commands. *")
  println("    *************************************")
  println()
  println(" GAPT Copyright (C) 2013")
  println(" This program comes with ABSOLUTELY NO WARRANTY. This is free")
  println(" software, and you are welcome to redistribute it under certain")
  println(" conditions; type `copying' for details.")
  println()
"""

  def main(args: Array[String]) {
    val f = File.createTempFile("cli-script", ".scala")
    f.deleteOnExit()
    val w = new BufferedWriter( new FileWriter(f) )
    w.write(script)
    w.close
    MainGenericRunner.main(Array("-usejavacp","-i",f.getCanonicalPath, "-Yrepl-sync"))
    sys.exit(0)
  }
}
