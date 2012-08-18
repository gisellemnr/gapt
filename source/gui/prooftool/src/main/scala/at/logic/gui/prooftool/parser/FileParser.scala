package at.logic.gui.prooftool.parser

/**
 * Created by IntelliJ IDEA.
 * User: mrukhaia
 * Date: 2/3/11
 * Time: 4:42 PM
 */

import scala.swing.Dialog
import java.io.{FileInputStream, InputStreamReader}
import java.util.zip.GZIPInputStream
import at.logic.parsing.language.xml.XMLParser.XMLProofDatabaseParser
import at.logic.parsing.readers.XMLReaders._
import at.logic.parsing.language.xml.ProofDatabase
import at.logic.parsing.calculi.xml.SimpleXMLProofParser
import at.logic.parsing.ParsingException
import at.logic.calculi.treeProofs.TreeProof
import at.logic.calculi.lk.base.types.FSequent
import at.logic.calculi.lk.base.LKProof
import at.logic.algorithms.shlk._
import at.logic.utils.ds.trees.Tree
import at.logic.language.hol.HOLFormula
import at.logic.gui.prooftool.gui.Main

class FileParser {

  def fileStreamReader(f: String) = new InputStreamReader(new FileInputStream(f), "UTF8")

  def gzFileStreamReader(f: String) = new InputStreamReader(new GZIPInputStream(new FileInputStream(f)), "UTF8")

  def ceresFileReader(input: InputStreamReader) {
    proofs = Nil
    structs = Nil
    proofdb = (new XMLReader(input) with XMLProofDatabaseParser).getProofDatabase()
  }

  def stabFileReader(input: InputStreamReader) {
    structs = Nil
    proofdb = new ProofDatabase(Map(), Nil, Nil, Nil)
    proofs = (new XMLReader(input) with SimpleXMLProofParser).getNamedTrees()
  }

  def lksFileReader(input: InputStreamReader) {
    proofs = Nil
    structs = Nil
    //  val start = System.currentTimeMillis()
    proofdb = new ProofDatabase(Map(), SHLK.parseProofs(input), Nil, Nil)
    //  val end = System.currentTimeMillis()
    //  println("parsing took " + (end - start).toString)
  }

  def parseFile(path: String): Unit = try {
    if (path.endsWith(".lks")) lksFileReader(fileStreamReader(path))
    else if (path.endsWith(".lks.gz")) lksFileReader(gzFileStreamReader(path))
    else if (path.endsWith(".xml")) try {
      ceresFileReader(fileStreamReader(path))
    } catch {
      case pe: ParsingException =>
        Main.questionMessage("There was a parsing exception:\n\n \t " + pe.getMessage + "\n\nContinue with another parser?") match {
          case Dialog.Result.Yes => stabFileReader(fileStreamReader(path))
          case _ =>
        }
    }
    else if (path.endsWith(".xml.gz")) try {
      ceresFileReader(gzFileStreamReader(path))
    } catch {
      case pe: ParsingException =>
        Main.questionMessage("There was a parsing exception:\n\n \t " + pe.getMessage + "\n\nContinue with another parser?") match {
          case Dialog.Result.Yes => stabFileReader(gzFileStreamReader(path))
          case _ =>
        }
    }
    else throw new Exception("Can not recognize file extension!")
    ProofToolPublisher.publish(ProofDbChanged)
  } catch {
    case err: AnyRef =>
      Main.errorMessage("Could not load file: " + path + "!\n\n" + err.toString.replaceAll(",", ",\n").replaceAll(">", ">\n"))
  }

  def addProofs(proofs: List[(String, LKProof)]) {
    proofdb = new ProofDatabase(proofdb.Definitions, proofdb.proofs ::: proofs, proofdb.axioms, proofdb.sequentLists)
  }

  def addSeqList(seqList: List[FSequent]) {
    addSeqList("sequentList ", seqList)
  }

  def addSeqList(name: String, seqList: List[FSequent]) {
    proofdb = new ProofDatabase(proofdb.Definitions, proofdb.proofs, proofdb.axioms,
      (name + proofdb.sequentLists.size.toString, seqList) :: proofdb.sequentLists)
  }

  def addStructTree(struct: Tree[_]) {
    structs = ("struct " + structs.size.toString, struct) :: structs
  }

  def addTrees(list: List[(String, Tree[_])]) {
    structs = list ::: structs
  }

  def getDefinitions: List[(HOLFormula, HOLFormula)] = proofdb.Definitions.toList //._1.toList ::: proofdb.Definitions._2.toList ::: proofdb.Definitions._3.toList

  def getSequentLists = proofdb.sequentLists

  def getProofs = proofdb.proofs ::: proofs

  def getProofDB = proofdb

  def getStructTrees = structs

  private var proofdb = new ProofDatabase(Map(), Nil, Nil, Nil)
  private var proofs: List[(String, TreeProof[_])] = Nil
  private var structs: List[(String, Tree[_])] = Nil
}
