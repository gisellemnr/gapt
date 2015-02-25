
package at.logic.gapt.provers.veriT

import at.logic.gapt.proofs.expansionTrees.ExpansionSequent
import scala.sys.process._
import java.io._
import at.logic.gapt.provers.Prover
import at.logic.gapt.io.veriT._
import at.logic.gapt.language.hol.HOLFormula
import at.logic.gapt.proofs.lk.base.FSequent

class VeriTProver extends Prover with at.logic.gapt.utils.traits.ExternalProgram {

  override def isValid( s: FSequent ): Boolean = {

    // Generate the input file for veriT
    val filename = "toProve" + System.currentTimeMillis + ".smt"
    val in_file = VeriTExporter( s, filename )

    // Execute the system command and get the result as a string.
    val result = try { ( "veriT " + filename ).!! }
    catch {
      case e: IOException => throw new Exception( "Error while executing VeriT." )
    }

    in_file.delete() match {
      case true  => ()
      case false => throw new Exception( "Error deleting smt file." )
    }

    val out_file = new File( "proof" + System.currentTimeMillis + ".smt" )
    val pw = new PrintWriter( out_file )
    pw.write( result )
    pw.close()

    // Parse the output
    val unsat = VeriTParser.isUnsat( out_file.getAbsolutePath() )
    out_file.delete() match {
      case true  => ()
      case false => throw new Exception( "Error deleting verit file." )
    }
    unsat
  }

  def getExpansionSequent( s: FSequent ): Option[ExpansionSequent] = {
    val smtBenchmark = File.createTempFile( "verit_input", ".smt" )
    smtBenchmark.deleteOnExit()

    VeriTExporter( s, smtBenchmark.getAbsolutePath )

    val output = s"veriT --proof=- --proof-version=1 $smtBenchmark".!!
    VeriTParser.getExpansionProof( new StringReader( output ) )
  }

  // VeriT proofs are parsed as Expansion Trees.
  // At the moment there is no method implemented that 
  // would generate an LK proof from an Expansion Tree.
  override def getLKProof( s: FSequent ) =
    throw new Exception( "It is not possible to generate LK proofs from VeriT proofs at the moment." )
  override def getLKProof( f: HOLFormula ) =
    throw new Exception( "It is not possible to generate LK proofs from VeriT proofs at the moment." )

  def isInstalled(): Boolean =
    try {
      "veriT --disable-banner".!!
      true
    } catch {
      case ex: IOException => false
    }
}
