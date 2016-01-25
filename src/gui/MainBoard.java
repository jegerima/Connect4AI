/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 *
 * @author Jefferson
 */
public class MainBoard extends javax.swing.JFrame {

    private GridLayout BOARD;
    private boolean player = true;
    private boolean playerYellow = true;
    private HashMap<String,TokenView> hash_tokens; //extracción del id del token 
    private String[][] matrix; //x+y ayuda a relacionar con hash
    private int[][] aiboard; //matriz de 1 y -1: -1 es para la PC y 1 para el humano
    private int columnMoves[]; // cantidad de movimiento  por columna
    private int DEFAULT_DEPTH = 2; //profundidad del árbol empezando desde al jugador  -> en realidad serían 3 de profundadid
    public static int BOARD_WIDTH = 8;
    public static int BOARD_HEIGHT = 8;
    
    //static final int MAX_DEPTH = 8;
    public final byte NOBODY = 0;
    public final byte PLAYER = 1;
    public final byte AI = -1;
    
    /**
     * Creates new form MainBoard
     */
    public MainBoard() {
        initComponents();
        //this.list_tokens = new ArrayList<>();
        this.matrix = new String[BOARD_HEIGHT][BOARD_WIDTH];
        this.aiboard = new int[BOARD_HEIGHT][BOARD_WIDTH];;
        this.columnMoves = new int[BOARD_HEIGHT];
        this.hash_tokens = new HashMap<>();
        this.BOARD = new GridLayout(8,8,0,0);
        this.pn_board.setLayout(BOARD);
      
        for(int i = 0; i<8; i++){
            columnMoves[i] = 0; //inicializo los movimientos para las 8 columnas 
            for(int j = 0; j<8; j++){
                final TokenView token = new TokenView(Color.WHITE,i,j);
                this.hash_tokens.put(token.getID(),token);
                this.pn_board.add(token, BorderLayout.CENTER);
                
                this.matrix[i][j] = token.getID();
                this.aiboard[i][j] = 0;
                token.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(!token.isHint()){
                            return;
                        }
                        
                            TokenView tokenAI = new TokenView();
                          
                            runPlayerMove(token);
                            if(hasWinner(token.getXindex(), token.getYindex(), 1)){
                                JOptionPane.showMessageDialog(null, "¡You win - Sometimes luck favors the dumbest!");
                                cleanBoard();
                                return;

                            }
                            tokenAI= testAIMove(token);
                            runAIMove(tokenAI);
                            if(hasWinner(tokenAI.getXindex(), tokenAI.getYindex(), -1)){
                                JOptionPane.showMessageDialog(null, "¡I win - I'm smarter than you!");
                                cleanBoard();
                                return;
                            }
                    }
                    
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if(token.isHint()){
                            token.setHover();
                        }
                    }
                    
                    @Override
                    public void mouseExited(MouseEvent e) {
                        if(token.isHint()){
                            token.deleteHover();
                        }
                    }
                });
            }
        }
        refreshBoard();
    }
    
    private void cleanBoard(){
        for (TokenView tk : hash_tokens.values()) {
            tk.reInit();
        }
        for (int i = 0; i < BOARD_HEIGHT; i++){
            for (int j = 0; j< BOARD_WIDTH; j++){
                aiboard[i][j] = 0;
            }
        }
        for (int i = 0; i < BOARD_HEIGHT; i++){
            columnMoves[i] = 0;
        }
        refreshBoard();
        System.out.println("CleanBoard performed");
    }
    
    public void refreshBoard(){
        for(int i=0; i<8; i++){
            for(int j=7; j>=0; j--){
                TokenView tk = hash_tokens.get(matrix[j][i]);
                if(tk.isLocked())
                    continue;
                else{
                    tk.setLikeHint();
                    break;
                }
            }
        }
        
        System.out.println(boardString());
    }
    
    private void runPlayerMove(TokenView tv){
        //TokenView tv = hash_tokens.get(xy);
        tv.setColorInGame(getPlayerColor());
        columnMoves[tv.getYindex()] = columnMoves[tv.getYindex()]+1;
        //System.out.println("Final Heuristic Player: "+getHeuristicValue(tv.getYindex(), player));
        player = !player;
        System.out.println("Player: Xindex: "+tv.getXindex()+" | Yindex: "+tv.getYindex());
        aiboard[tv.getXindex()][tv.getYindex()] = 1; // el jugador le dio click a ese token
        refreshBoard();
    }
    
    private void runAIMove(TokenView tv){
        tv.setColorInGame(getAIColor(
        ));
        columnMoves[tv.getYindex()] = columnMoves[tv.getYindex()]+1;
        System.out.println("AI: Final Heuristic AI: "+getHeuristicValue(tv.getYindex(), player));
        player = !player;
        System.out.println("Xindex: "+tv.getXindex()+" | Yindex: "+tv.getYindex());
        aiboard[tv.getXindex()][tv.getYindex()] = -1;
        refreshBoard();
    }
    
    //*********************** SON LOS MOVIMIENTOS FICITICIOS PARA IR RECORRIENDO EL ARBOL ***************************
        
    private void makePlayerMove(int column){
        int row = (BOARD_HEIGHT-1)-columnMoves[column];
        aiboard[row][column] = 1;
        columnMoves[column] = columnMoves[column] + 1;
        System.out.println("Perfomed player move in: ("+row+","+column+")");
    }
    
    private void makeAIMove(int column){
        int row = (BOARD_HEIGHT-1)-columnMoves[column];
        aiboard[row][column] = -1;
        columnMoves[column] = columnMoves[column] + 1;
        System.out.println("Perfomed AI move in: ("+row+","+column+")");
    }
    
    private void undoPlayerMove(int column){
        int row = (BOARD_HEIGHT)-columnMoves[column];
        aiboard[row][column] = 0;
        columnMoves[column] = columnMoves[column] - 1;
        System.out.println("Undo player move in: ("+row+","+column+")");
    }
    
    private void undoAIMove(int column){
        int row = (BOARD_HEIGHT)-columnMoves[column];
        aiboard[row][column] = 0;
        columnMoves[column] = columnMoves[column] - 1;
        System.out.println("Undo AI move in: ("+row+","+column+")");
    }
    
    
    // *************************************** FUNCIONES SENCILLAS **********************************************
    
    private boolean hasWinner(int x, int y, int ficha){
        
        int hrv=0, hlv=0, vdv=0, vuv=0, nev=0, sev=0, swv=0, nwh=0;    
        //Right
        if(y<=(BOARD_WIDTH-4) && y >= 0){
            for(int i=y; i<=y+3;i++){
                if(aiboard[x][i]==ficha){
                    hrv++;
                }
            }
            if(hrv==4) return true;
        }
        
        //Left
        if(y>=(BOARD_WIDTH-5)&& y<=(BOARD_WIDTH-1)){
            for(int i=y; i>=y-3;i--){
                if(aiboard[x][i]==ficha){
                    hlv++;
                }
            }
             if(hlv==4) return true;
        }
        
        //Down
        if(x >= 0 && x<=(BOARD_WIDTH-4)){
            for(int i=x; i<=x+3;i++){
                if(aiboard[i][y]==ficha){        //Nobody has played that position
                    vdv++;
                }
            }
            if(vdv==4) return true;
        }
        
        //Up
        if(x<=(BOARD_WIDTH-1) && x>=(BOARD_WIDTH-5)){
            for(int i=x; i>=x-3;i--){               
                if(aiboard[i][y]==ficha){        //Nobody has played that position
                    vuv++;
                }
            }
            if(vuv==4) return true;
        }
       
      
        //RightUp
        if (x<=(BOARD_WIDTH-1) && x>=(BOARD_WIDTH-5) && y<=(BOARD_WIDTH-4) && y >= 0 ){

            int j = y;
            for(int i=x; i>=x-3;i--){
                
                if(aiboard[i][j]==ficha){        //Nobody has played that position
                    nev++;
                }
                j++;
            }
            if (nev==4) return true;
        }
        
        //RightDown
        if(x >= 0 && x<=(BOARD_WIDTH-4) && y<=(BOARD_WIDTH-4) && y >= 0){
            int j = y;
            for(int i=x; i<=x+3;i++){             
                if(aiboard[i][j]==ficha){        //Nobody has played that position
                    sev++;
                }
               j++;
            }
            if(sev == 4) return true;
        }
        
        
        //LeftDown
        if(x >= 0 && x<=(BOARD_WIDTH-4) && y>=(BOARD_WIDTH-5)&& y<=(BOARD_WIDTH-1)){
            int j = y;
            for(int i=x; i<=x+3;i++){
               if(aiboard[i][j]==ficha){        //Nobody has played that position
                    swv++;
               }
                j--;
            }
            if(swv==4) return true;
        }
        
        
        //LeftUp
        if(x<=(BOARD_WIDTH-1) && x>=(BOARD_WIDTH-5) && y>=(BOARD_WIDTH-5)&& y<=(BOARD_WIDTH-1)){
            int j = y;
            for(int i=x; i>=x-3;i--){                            
                if(aiboard[i][j]==ficha){        //Nobody has played that position
                  
                    nwh++;
                }
                j--;
            }
            if(nwh==4) return true;
        }
        
    return false;
    }
    
    private Color getPlayerColor(){
        return playerYellow ? Color.YELLOW : Color.RED;
    }
    
    private Color getAIColor(){
        return playerYellow ? Color.RED :Color.YELLOW;
    }
    
    boolean isValidMove(int column){
        return getTokensInColumn(column) < BOARD_HEIGHT;
    }
    
    boolean isAIValidMove(int column){        
        return columnMoves[column] < BOARD_HEIGHT;
    }
    
    public String genID(int x, int y){
        return Integer.toString(x)+Integer.toString(y);
    }
    
    int getTokensInColumn(int column){
        int n_tokens = 0;
        for(int i=(BOARD_HEIGHT-1); i>=0; i--){
            if(hash_tokens.get(genID(i, column)).isLocked()){
                n_tokens++;
            }else{
                break;
            }
        }
        return n_tokens;
    }
    
    double getHeuristicValue(int column, boolean player){
        //int val = player?1:-1;
        int tmpx = (BOARD_HEIGHT)-columnMoves[column];    //Row
        final int y = column;                 //Column
        try{
        int val = player?-1:1;
        
        //System.out.println(columnMoves[column]);
        
        if(tmpx==8 && player)
            tmpx =7;
        else if(tmpx==8) return 0;
        
        final int x = tmpx;
        
        
                
        //System.out.println("Position: ("+x+","+y+"), Player: "+player);
        
        int hlv_p = 0;      //Horizontal left value
        int hlv_n = 0;
        int hrv_p = 0;      //Horizontal right value
        int hrv_n = 0;
        
        int vuv_p = 0;      //Horizontal up value
        int vuv_n = 0;
        int vdv_p = 0;      //Horizontal down value
        int vdv_n = 0;
        
        int nev_p = 0;      //Right Up Corner
        int nev_n = 0;
        int swv_p = 0;      //Left Down Corner
        int swv_n = 0;
        
        int sev_p = 0;      //Right Down Corner
        int sev_n = 0;
        int nwv_p = 0;      //Left Down Corner
        int nwv_n = 0;
        
        int hwl = 0;
        int hwr = 0;
        int vwd = 0;
        
        //Right
        if(y<(BOARD_WIDTH-1)){
            boolean pro = false;    //A favor
            boolean against = false;
            for(int i=y; i<y+3;i++){
                if(i>=(BOARD_WIDTH-1))break;
                                
                if(aiboard[x][i+1]==0){        //Nobody has played that position
                    //break;
                    hwr++;
                }else if(aiboard[x][i+1]==val){
                    if(against)break;
                    pro = true;
                    hrv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    hrv_n--;
                }
            }
        }
        
        //Left
        if(y>0){
            boolean pro = false;    //A favor
            boolean against = false;
            for(int i=y; i>=y-3;i--){
                if((i-1)<0)break;
                                
                if(aiboard[x][i-1]==0){        //Nobody has played that position
                    //break;
                    hwl++;
                }else if(aiboard[x][i-1]==val){
                    if(against)break;
                    pro = true;
                    hlv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    hlv_n--;
                }
            }
        }
        
        //Down
        if(x<(BOARD_WIDTH-1)){
            boolean pro = false;    //A favor
            boolean against = false;
            for(int i=x; i<x+3;i++){
                if(i>=(BOARD_WIDTH-1))break;
                                
                if(aiboard[i+1][column]==0){        //Nobody has played that position
                    //break;
                    vwd++;
                }else if(aiboard[i+1][column]==val){
                    if(against)break;
                    pro = true;
                    vdv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    vdv_n--;
                }
            }
        }
        /*
        //Up
        if(x>0){
            boolean pro = false;    //A favor
            boolean against = false;
            for(int i=x; i>=x-2;i--){
                if((i-1)<0)break;
                                
                if(aiboard[i-1][column]==0){        //Nobody has played that position
                    break;
                }else if(aiboard[i-1][column]==val){
                    if(against)break;
                    pro = true;
                    vuv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    vuv_n--;
                }
            }
        }
        */
        
        //RightUp
        if(y<(BOARD_WIDTH-1) && x > 0){
            boolean pro = false;    //A favor
            boolean against = false;
            int j = column;
            for(int i=x; i>=x-3;i--){
                if((i-1)<0 || j>=(BOARD_WIDTH-1))break;
                                
                if(aiboard[i-1][j+1]==0){        //Nobody has played that position
                    break;
                }else if(aiboard[i-1][j+1]==val){
                    if(against)break;
                    pro = true;
                    nev_p++;
                }else{
                    if(pro) break;
                    against = true;
                    nev_n--;
                }
                j++;
            }
        }
        
        //RightDown
        if(y<(BOARD_WIDTH-1) && x < (BOARD_WIDTH-1)){
            boolean pro = false;    //A favor
            boolean against = false;
            int j = column;
            for(int i=x; i<x+3;i++){
                if(i>=(BOARD_WIDTH-1) || j>=(BOARD_WIDTH-1))break;
                                
                if(aiboard[i+1][j+1]==0){        //Nobody has played that position
                    break;
                }else if(aiboard[i+1][j+1]==val){
                    if(against)break;
                    pro = true;
                    sev_p++;
                }else{
                    if(pro) break;
                    against = true;
                    sev_n--;
                }
                j++;
            }
        }
        
        //LeftDown
        if(y>0 && x < (BOARD_WIDTH-1)){
            boolean pro = false;    //A favor
            boolean against = false;
            int j = column;
            for(int i=x; i<x+3;i++){
                if(i>=(BOARD_WIDTH-1) || (j-1)<0)break;
                                
                if(aiboard[i+1][j-1]==0){        //Nobody has played that position
                    break;
                }else if(aiboard[i+1][j-1]==val){
                    if(against)break;
                    pro = true;
                    swv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    swv_n--;
                }
                j--;
            }
        }
        
        //LeftUp
        if(y>0 && x > 0){
            boolean pro = false;    //A favor
            boolean against = false;
            int j = column;
            for(int i=x; i>=x-3;i--){
                if((i-1)<0 || (j-1)<0)break;
                                
                if(aiboard[i-1][j-1]==0){        //Nobody has played that position
                    break;
                }else if(aiboard[i-1][j-1]==val){
                    if(against)break;
                    pro = true;
                    nwv_p++;
                }else{
                    if(pro) break;
                    against = true;
                    nwv_n--;
                }
                j--;
            }
        }
                
        int max = Math.max((hlv_p+hrv_p), (vuv_p+vdv_p));
        max = Math.max(max, (nev_p+swv_p));
        max = Math.max(max, (sev_p+nwv_p));
        
        int min = Math.min((hlv_n+hrv_n),(vuv_n+vdv_n));
        min = Math.min(min,(nev_n+swv_n));
        min = Math.min(min,(sev_n+nwv_n));
        
        System.out.println("MaxF: "+max + "|MinF: "+min);
        
        double extraAI = 0.0;
        double extraPlayer = 0.0;
               
        if(player){ //Computador
            //if(max == 3) return 500;
            extraAI = 0.5;
        }else{
            //if(min ==3) return 500;
            extraPlayer = -0.5;
        }
        
        double fact1 = 1.0 + extraAI;
        double fact2 = 1.0 + extraPlayer;
        
        if(max == 3 || min ==-3){
            System.out.println("TAPANDO 3");
            System.out.println("Horizontal: "+(hlv_p+hrv_p)+"|"+(hlv_n+hrv_n));
            System.out.println("Vertical: "+vdv_p+"|"+vdv_n);
            System.out.println("Diag. princ: "+(nev_p+swv_p)+"|"+(nev_n+swv_n));
            System.out.println("Otra diagonal: "+(sev_p+nwv_p)+"|"+(sev_n+nwv_n)); 
            if(max==3) fact1 = fact1*2.0;
            if(min == -3) fact2 = fact2*2.0;
            
            //if(max == 3 && min ==-3)
            //    return 500;
            
            if(max == 3)
                return 500+DEFAULT_DEPTH+1;
            if(min == -3)
                return 500+DEFAULT_DEPTH;
        }     
        System.out.println(boardString());

        return ((double)max*fact1 + Math.abs((double)min*fact2)); //+ hwl + hwr + vwd;
                        
        }catch(Exception e){
            System.out.println("Excepcion en getHeuristicValue ("+tmpx+","+y+")");
            e.printStackTrace();
        }
        return 0;
        
    }
    
    
    // FUNCIÓN QUE RETORNA EL TOKEN QUE DEBE DAR LA PC
    
    private TokenView testAIMove(TokenView tk){
        double MAX = 500; // ES ALPHA PARA LA PC
        double MIN = -500; // ES BETA PARA EL JUGADOR
        int x = 7;
        int y = 0;
        double val = Double.MAX_VALUE;
        double maxval = 500;
        double values[] = new double[8]; //las heuristicas acumuladas de los nodos hijos de cada una de las 8 opciones del
        // turno de la PC
        double hs[] = new double [8]; // la heuristica de la 1era capa de la PC 
        
        ArrayList<String> l_values = new ArrayList<>();
        ArrayList<String> l = new ArrayList<>();
        
        ArrayList<IDValue> ab_h = new ArrayList<>();
        ArrayList<IDValue> c_h = new ArrayList<>();
        
        GNode master = new GNode("--", tk.getID(), 0, 0);
        
        for(int i = 0; i< BOARD_HEIGHT; i++){
            makeAIMove(i); System.out.println("*********************************");
            hs[i] = getHeuristicValue(i, true)+0.1;
            GNode child = new GNode(master.nodeID, ((BOARD_HEIGHT-1) - columnMoves[i])+""+i, 1, hs[i]);
            double tmp = Math.abs(alphabeta(DEFAULT_DEPTH, MAX, MIN, player, i, child));
            master.addChild(child);
            undoAIMove(i); System.out.println("*********************************");
            //double tmp = alphabeta(2, -3, 3, player, i);
            System.out.println("=======================================");
            System.out.println("tmp:"+i+" "+tmp+" | value:"+val);
            System.out.println("=======================================");
            values[i] = (tmp);
            
            
            
            x = (BOARD_HEIGHT-1) - columnMoves[i];
            y = i;
            
            //ab_h.add(new IDValue(x+y+"", tmp));
            //c_h.add(new IDValue(x+y+"", hs[i]));
            
            
            
            if(tmp > maxval){
                l.clear();
                maxval = tmp;
                l_values.add(""+x+y+"");
            }
                
            if(val == tmp){
                l.add(""+x+y+"");
            }
            
            if(tmp < val){
                l.clear();
                //x = (BOARD_HEIGHT-1) - columnMoves[i];
                //y = i;
                val = (tmp);
                l.add(""+x+y+"");
            }
        }
        
        System.out.println("Token maximixed id: "+x+y+"");
        
        //Collections.sort(l);
        try{
        GGraph.draw(master);
        }catch(Exception e){
            System.out.println("ERROR");
            e.printStackTrace();
        }
        
        
        for(int w = 0; w<8;w++){
            System.out.println(values[w] + "|"+ hs[w]);
        }
        
        
        
        for(int w = 0; w<8;w++){
            if(hs[w]>values[w])
                return hash_tokens.get(((BOARD_HEIGHT-1) - columnMoves[w])+""+w);
        }
        
        if(l_values.size()>0){
            System.out.println(l_values.size());
            return hash_tokens.get(l_values.get(Math.abs(new Random().nextInt())%l_values.size()));
        }
        
        System.out.println("size:"+l.size());
        int index = Math.abs(new Random().nextInt())%l.size();
        System.out.println("index: "+index+" size:"+l.size());
        //return hash_tokens.get(""+x+y+"");
        System.out.println(l.get(index));
        return hash_tokens.get(l.get(index));
    }
    
    
    double alphabeta(int depth, double alpha, double beta, boolean maximizingAI, int col, GNode parent) {
          //boolean hasWinner = board.hasWinner();
          
          // All these conditions lead to a
          // termination of the recursion
          if (depth == 0 ) {
              /*
              double score = 0;
              if (hasWinner) {
                  score = board.playerIsWinner() ? LOSE_REVENUE : WIN_REVENUE;
             } else {
                 score = UNCERTAIN_REVENUE;
             }
             // Note that depth in this
             // implementation starts at a high
             // value and is decreased in every
             // recursive call. This means that the
             // deeper the recursion is, the
             // greater MAX_DEPTH – depth will
             // become and thus the smaller the
             // result will become.
             // This is done as a tweak, simply
             // spoken, something bad happening in
             // the next turn is worse than it
             // happening in let’s say five steps.
             // Analogously something good
             // happening in the next turn is
             // better than it happening in five
             // steps.
             return (score / (MAX_DEPTH - depth + 1));
              */

              double h = getHeuristicValue(col, maximizingAI);
              System.out.println("RETURNED h: "+h+ " for ("+(8-columnMoves[col])+","+col+") | MaximizingAI: "+maximizingAI );
              return h;
         }

         double lastAlpha = alpha;
         double lastBeta = beta;
         
         if (maximizingAI) {
             double currentAlpha = 0;
             for (int column = 0; column < BOARD_WIDTH; column++) {
                 if (isAIValidMove(column)) {
                     /*
                     double currentHeuristic = Math.abs(getHeuristicValue(column, true));
                     if(currentHeuristic>=alpha){
                         return currentHeuristic;
                     }
                     */
                     
                     makeAIMove(column);
                     GNode child = new GNode(parent.nodeID, ((BOARD_HEIGHT-1) - columnMoves[column])+""+column, 2+(DEFAULT_DEPTH-depth), 0);
                     double newAlpha = Math.abs(alphabeta(depth-1, alpha, beta,false ,column,child));
                     child.hvalue = newAlpha;
                     parent.addChild(child);
                     //double lastAlpha = alpha;
                     alpha = Math.max(alpha, (newAlpha));
                     currentAlpha = Math.max(currentAlpha,newAlpha);
                     //alpha = Math.max(alpha, alphabeta(depth – 1, alpha, beta,false));
                     System.out.println("MAX | lastAplha: "+ lastAlpha+ " currentAlpha: "+currentAlpha+" alpha: "+alpha +" > beta: "+beta + "| AI h: "+newAlpha +" on ("+(8-columnMoves[column])+","+column+")| depth: "+depth);
                     undoAIMove(column);
                     
                     
                     if(newAlpha>alpha){
                         System.out.println("BREAKED NEW ALPHA");
                         alpha = alpha - (DEFAULT_DEPTH-depth);
                         System.out.println("============= Returned alpha:" + alpha);
                         return alpha;
                     }
                 }
             }
             currentAlpha = currentAlpha - (DEFAULT_DEPTH-depth);
             System.out.println("============= Returned alpha:" + currentAlpha);
             //return alpha;
             return currentAlpha;
         } else {
             double currentBeta = 0;
             for (int column = 0; column < BOARD_WIDTH; column++) {
                 if (isAIValidMove(column)) {
                     /*
                     double currentHeuristic = 0-getHeuristicValue(column, false);
                     if(currentHeuristic<=beta){
                         System.out.println("BY CURRENT HEURISTIC");
                         return currentHeuristic;
                     }
                     */
                     
                     makePlayerMove(column);
                     GNode child = new GNode(parent.nodeID, ((BOARD_HEIGHT-1) - columnMoves[column])+""+column, 2+(DEFAULT_DEPTH-depth), 0);
                     double newBeta = 0-alphabeta(depth-1,alpha,beta,true,column,child);
                     child.hvalue = newBeta;
                     parent.addChild(child);
                     
                     beta = Math.min(beta,newBeta);
                     currentBeta = Math.min(newBeta, currentBeta);
                     
                     //beta = Math.min(beta,alphabeta(depth – 1,alpha, beta, true));
                     System.out.println("MIN | lastBeta: "+ lastBeta+ " currentBeta"+currentBeta+" alpha: "+alpha +" > beta: "+beta + "| Player h: "+newBeta+" on ("+(8-columnMoves[column])+","+column+") | depth: "+depth);
                     undoPlayerMove(column);
                     
                     if(newBeta < beta){
                         System.out.println("BREAKED NEWBETA ");
                         beta = beta + (DEFAULT_DEPTH-depth);
                         System.out.println("============== Returned beta: " + beta);
                         return beta;
                     }                 
                 }
             }
             currentBeta = (currentBeta) + (DEFAULT_DEPTH-depth);
             System.out.println("============== Returned beta: " + currentBeta);
             //return beta;
             return currentBeta;
         }
     }
    
    
    //====================== Utils ======================

    private void sleep(int milis){
        try {
            Thread.sleep(milis);                 //1000 milliseconds is one second.
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }  
    }
  
    public String boardString() {
		StringBuffer result = new StringBuffer();
		for (int x = 0; x < BOARD_WIDTH; x++) {
			result.append((x + 1) + " ");
		}
		result.append(System.lineSeparator());
		for (int y = 0; y < BOARD_HEIGHT; y++) {
			for (int x = 0; x < BOARD_WIDTH; x++) {
				if (aiboard[y][x] == PLAYER) {
					result.append("X ");
				} else if (aiboard[y][x] == AI) {
					result.append("O ");
				} else {
					result.append(". ");
				}
			}
			result.append(System.lineSeparator());
		}
		return result.toString();
	}
    
    public class GNode{
        public String parentID = "";
        public String nodeID = "";
        public int depth = 0;
        public double hvalue = 0.0;
        ArrayList<GNode> children;
        
        public GNode(String pid, String nid, int d, double h){
            this.parentID = pid;
            this.nodeID = nid;
            this.depth = d;
            this.hvalue = h;
            children = new ArrayList<>();
        }
        
        public void addChild(GNode n){
            children.add(n);
        }
        
        public ArrayList<GNode> getChildren(){
            return this.children;
        }
    }
    
    class IDValue{
        public String position;
        public double value;
    
        public IDValue(String position, double value){
            position = position;
            value = value;
        }
    }
    
    public class IDValueComparator implements Comparator<IDValue> {
    @Override
    public int compare(IDValue o1, IDValue o2) {
        return Double.compare(o1.value, o2.value);
    }
}

    
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
// ================================================================================
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mni_help = new javax.swing.JMenuItem();
        pnl_general_info = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        pn_board = new javax.swing.JPanel();
        jSeparator3 = new javax.swing.JSeparator();
        mn_bar = new javax.swing.JMenuBar();
        mn_options = new javax.swing.JMenu();
        mni_new_game = new javax.swing.JMenuItem();
        mni_chk_hint = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mni_exit = new javax.swing.JMenuItem();
        mn_about = new javax.swing.JMenu();

        mni_help.setText("Ayuda");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Connect4 - IA Games");

        javax.swing.GroupLayout pnl_general_infoLayout = new javax.swing.GroupLayout(pnl_general_info);
        pnl_general_info.setLayout(pnl_general_infoLayout);
        pnl_general_infoLayout.setHorizontalGroup(
            pnl_general_infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        pnl_general_infoLayout.setVerticalGroup(
            pnl_general_infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 55, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pn_boardLayout = new javax.swing.GroupLayout(pn_board);
        pn_board.setLayout(pn_boardLayout);
        pn_boardLayout.setHorizontalGroup(
            pn_boardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        pn_boardLayout.setVerticalGroup(
            pn_boardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);

        mn_options.setText("Opciones");

        mni_new_game.setText("Nuevo juego");
        mni_new_game.setActionCommand("mni_new_game");
        mni_new_game.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_new_gameActionPerformed(evt);
            }
        });
        mn_options.add(mni_new_game);

        mni_chk_hint.setSelected(true);
        mni_chk_hint.setText("Mostrar ayuda");
        mn_options.add(mni_chk_hint);
        mn_options.add(jSeparator1);

        mni_exit.setText("Salir");
        mn_options.add(mni_exit);

        mn_bar.add(mn_options);

        mn_about.setText("Acerca de");
        mn_bar.add(mn_about);

        setJMenuBar(mn_bar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl_general_info, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(pn_board, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 135, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_general_info, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pn_board, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 415, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mni_new_gameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_new_gameActionPerformed
        cleanBoard();
    }//GEN-LAST:event_mni_new_gameActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainBoard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainBoard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JMenu mn_about;
    private javax.swing.JMenuBar mn_bar;
    private javax.swing.JMenu mn_options;
    private javax.swing.JCheckBoxMenuItem mni_chk_hint;
    private javax.swing.JMenuItem mni_exit;
    private javax.swing.JMenuItem mni_help;
    private javax.swing.JMenuItem mni_new_game;
    private javax.swing.JPanel pn_board;
    private javax.swing.JPanel pnl_general_info;
    // End of variables declaration//GEN-END:variables
}
