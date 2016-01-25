/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import gui.MainBoard.GNode;
import javax.swing.JFrame;

/**
 *
 * @author Jefferson
 */
public class GGraph extends JFrame{

    /**
     * @param args the command line arguments
     */
    
    private static final long serialVersionUID = -2707712944901661771L;

    public GGraph(GNode node, int w, int h)
    {
            super("Hello, World!");

            mxGraph graph = new mxGraph();
            Object parent = graph.getDefaultParent();

            graph.getModel().beginUpdate();
            try{
                    Object v0 = graph.insertVertex(parent, null, node.nodeID+'\n'+node.hvalue, 630, 30, 20,40);
                    createRecursiveGraph(v0, node, graph,parent,0);
                    
                    //Object v11 = graph.insertVertex(parent, null, "World!", 80, 150,80, 30);
                    //Object v12 = graph.insertVertex(parent, null, "Hello!", 240, 150,80, 30);
                    //graph.insertEdge(parent, null, "Edge", v0, v11);
                    //graph.insertEdge(parent, null, "Edge", v0, v12);
            }finally{
                    graph.getModel().endUpdate();
            }

            mxGraphComponent graphComponent = new mxGraphComponent(graph);
            this.add(graphComponent);
    }
    
    private void createRecursiveGraph(Object vertex, GNode pnode, mxGraph graph, Object parent,int offset){
        for(int i=0; i<pnode.getChilds().size();i++){
            GNode gn = pnode.getChilds().get(i);
            
            int currentDepth = gn.depth;
            if(currentDepth>2)return;
            int off = (int)(Math.pow(8, currentDepth));
            //Object newVertex = graph.insertVertex(parent, null, gn.nodeID, ((1280/off-(20-2*currentDepth))*i)+(1280/off)+((currentDepth-1)*offset*off) , 30 + (20+150)*currentDepth, 20, 20);
            Object newVertex = graph.insertVertex(parent, null, gn.nodeID+'\n'+gn.hvalue, ((1280/off)*i)+  (1280/off*offset*7)+(1280/off*offset) + (1280/off/2)*(offset+1) , 30 + (20+150)*currentDepth, 20-(2*currentDepth), 40);
            graph.insertEdge(parent, null, gn.hvalue, vertex, newVertex);
            
            
            
            //System.out.println(currentDepth);
            createRecursiveGraph(newVertex, gn, graph, parent,i);
        }
    }
        
        
    public static void draw(GNode node) {
        int height = 1440;
        int width = 2520;
        
        GGraph frame = new GGraph(node, width, height);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	frame.setSize(width, height);
        frame.setVisible(true);
    }
    
}
