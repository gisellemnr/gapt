package at.logic.gui.prooftool.gui

// The code in this file displays expansion sequents.

import swing._
import java.awt.{Dimension, Font, Color}
import java.awt.Font._
import swing.event.UIElementResized
import at.logic.calculi.expansionTrees._
import event.MouseClicked
import java.awt.event.MouseEvent
import scala.collection.mutable.ListBuffer

class DrawExpansionSequent(val ExpSequent: ExpansionSequent, private val fSize: Int) extends SplitPane(Orientation.Vertical) {
  background = new Color(255,255,255)
  private val ft = new Font(SANS_SERIF, PLAIN, fSize)
  //private val width = toolkit.getScreenSize.width - 150
  //private val height = toolkit.getScreenSize.height - 150
  preferredSize = calculateOptimalSize
  dividerLocation = preferredSize.width / 2
  val antecedent = new ListBuffer[DrawExpansionTree]
  val succedent = new ListBuffer[DrawExpansionTree]
  leftComponent = side(ExpSequent.antecedent, "Antecedent", ft)
  rightComponent = side(ExpSequent.succedent, "Succedent", ft)

  listenTo(Main.top)
  reactions += {
    case UIElementResized(Main.top) =>
      preferredSize = calculateOptimalSize
      revalidate()
  }

  def calculateOptimalSize = {
    val width = Main.top.size.width
    val height = Main.top.size.height
    if (width > 100 && height > 200)
      new Dimension(Main.top.size.width - 70, Main.top.size.height - 150)
    else new Dimension(width, height)
  }

  def side(expTrees: Seq[ExpansionTree], label: String, ft: Font) = new BoxPanel(Orientation.Vertical) {
    contents += new Label(label) {
      font = ft.deriveFont(Font.BOLD)
      opaque = true
      border = Swing.EmptyBorder(10)
      listenTo(mouse.clicks)
      reactions += {
        case e: MouseClicked if e.peer.getButton == MouseEvent.BUTTON3 =>
          PopupMenu(DrawExpansionSequent.this, this, e.point.x, e.point.y)
      }
    }
    contents += new ScrollPane {
      peer.getVerticalScrollBar.setUnitIncrement( 20 )
      peer.getHorizontalScrollBar.setUnitIncrement( 20 )
      contents = new BoxPanel(Orientation.Vertical) {
        background = new Color(255,255,255)
        expTrees.foreach( f => {val det = draw(f); contents += det; if (label == "Antecedent") antecedent += det else succedent += det} )
      }
    }
  }

  def draw(et: ExpansionTree) = {
    val comp = new DrawExpansionTree(et,ft)
    comp.border = Swing.EmptyBorder(10)
    comp
  }
}
