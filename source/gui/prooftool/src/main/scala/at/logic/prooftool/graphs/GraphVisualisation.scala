package at.logic.prooftool.graphs

import at.logic.utils.ds._

import org.jgraph.JGraph
import org.jgraph.graph.AttributeMap
import org.jgraph.graph.GraphLayoutCache

import javax.swing._
import java.awt.geom._
import java.awt._
import java.util._

import org.jgrapht._
import org.jgrapht.ext._
import org.jgrapht.graph._

import at.logic.calculi.lk._
import at.logic.calculi.lk.base._
import at.logic.language.hol.propositions._
import at.logic.language.lambda.typedLambdaCalculus._


object VisualisationUtils {

    //generates a binary tree of given depth and name of parent label. subsequnt nodes will get
    // the character l or r prepended depending wether they went _l_eft ot _r_ight
    def createTree(label:String, depth : Int) : at.logic.utils.ds.graphs.Graph[String] = {
        if (depth <= 0) {
            graphs.VertexGraph[String](label, graphs.EmptyGraph[String])
        } else {
            var label1 = "l"+label
            var label2 = "r"+label
            var tree1 = createTree(label1, depth-1)
            var tree2 = createTree(label2, depth-1)

            var g : graphs.Graph[String] = graphs.VertexGraph(label,graphs.UnionGraph(tree1,tree2))
            g = graphs.EdgeGraph[String](label,label1,g )
            g = graphs.EdgeGraph[String](label,label2,g )
            g
        }
    }

    //formats a lambda term to a readable string, dropping the types and printing binary function symbols infix
    def formulaToString(f:LambdaExpression) : String = {
        f match {
            case App(App(Var(name,t),x),y)    => "(" + formulaToString(x) + " "+ name.toString()+ " " +formulaToString(y) +")"
            case App(x,y)    => formulaToString(x) + "("+ formulaToString(y) +")"
            case Var(name,t) => name.toString()
            case Abs(x,y)    => formulaToString(x)+".("+formulaToString(y)+")"
            case  x : Any    => "(unmatched class: "+x.getClass() + ")"
                //            case _ => "(argl!!!)"
        }
    }

    // formats a sequent to a readable string
    def sequentToString(s : Sequent) : String = {
        var sb = new scala.StringBuilder()
        var first = true
        for (f <- s.antecedent) {
            if (! first) sb.append(", ")
            else first = false

            sb.append(formulaToString(f))
        }
        sb.append(" :- ")
        first =true
        for (f <- s.succedent) {
            if (! first) sb.append(", ")
            else first = false
            sb.append(formulaToString(f))
            
        }
        sb.toString
    }

    // formats a graph to dot format (http://graphviz.org)
    def toDotFormat(g : graphs.Graph[SequentOccurrence]) : String = {
        var sb = new scala.StringBuilder()
        var m = new scala.collection.mutable.HashMap[SequentOccurrence,Int]()

        sb.append("digraph g { \n")
        // output vertices
        val vs = g.graph.vertexSet()
        val it = vs.iterator
        var v: SequentOccurrence = null
        var i = 0
            
        while (it.hasNext) {
            v = it.next
            m.put(v,i)
            sb.append("\tv"+i+ " [label=\""+sequentToString(v.getSequent)+"\"];\n")
            i += 1
        }

        sb.append("\n")
        // output edges
        val es = g.graph.edgeSet()
        val it2 = es.iterator
        var e: DefaultEdge[SequentOccurrence] = null
        i = 0
            
        while (it2.hasNext) {
            e = it2.next
            (m.get(g.graph.getEdgeSource(e)), m.get(g.graph.getEdgeTarget(e))) match {
              case (Some(src), Some(trg)) =>
                sb.append("\t v"+src + " -> v"+ trg + ";\n")

              case _ => ;
            }
            i += 1
        }
        
        sb.append("\n}\n")
        

        sb.toString
    }


    /*
    def placeNodes(jgraph : JGraph) = {
        // hm this should work, shouldn't it?
        Console.println("placement")


        var cache : GraphLayoutCache  = jgraph.getGraphLayoutCache();
        var m  = cache.createNestedMap();
        var it  = m.values().iterator();
        var i : Any = null;

        while (it.hasNext()) {
            i = it.next();

            if (i.isInstanceOf[Map[Any,Any] ]) {
                var im  = i.asInstanceOf[Map[Any,Any] ];
                var j : Any = null;
                var it2 = im.values().iterator()

                while (it2.hasNext() ) {
                    j = it2.next();

                    if (j.isInstanceOf[AttributeMap.SerializableRectangle2D]) {
                        var  r :Rectangle2D.Double = j.asInstanceOf[AttributeMap.SerializableRectangle2D];
                        r.x = 10.0;
                        r.y = 400.0;
                        r.width = 100.0;
                        r.height = 50.0;
                        Console.println("setting new rectangle:"+r.toString());
                    }
                }
            }
        }

        Console.println("cache partial? "+cache.isPartial)
            
        cache.edit(m);

    }   */
}
