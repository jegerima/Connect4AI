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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 *
 * @author Jefferson
 */
public class MainBoard extends javax.swing.JFrame {
    
    private int player_wins = 0;
    private int ai_wins = 0;
    private boolean ai_finish;

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
        
        ImageIcon ii = new ImageIcon(getClass().getResource("wait1.gif"));
        lbl_gif.setIcon(ii);
        lbl_gif.setVisible(false);
      
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
                                player_wins++;
                                lbl_player_wins.setText(player_wins+"");
                                return;

                            }
                            
                            ai_finish = false;
                            Thread t = new Thread(new Runnable() {
                            @Override
                                public void run() {
                                    lbl_gif.setVisible(true);
                                    while(!ai_finish){
                                        sleep(50);
                                    }
                                    lbl_gif.setVisible(false);
                                }
                            }); t.start();
                            
                            
                            tokenAI= testAIMove(token);
                            runAIMove(tokenAI);
                            ai_finish = true;
                            
                            lbl_gif.setVisible(false);
                            if(hasWinner(tokenAI.getXindex(), tokenAI.getYindex(), -1)){
                                JOptionPane.showMessageDialog(null, "¡I win - I'm smarter than you!");
                                cleanBoard();
                                ai_wins++;
                                lbl_ai_wins.setText(ai_wins+"");
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
                    //if(mni_chk_hint.isSelected())
                    //    tk.setLikeHint();
                    //else
                        tk.setLikeHint();
                        //tk.setLikeHintWithoutColor();
                    break;
                }
            }
        }
        
        System.out.println(boardString());
    }
    
    private void runPlayerMove(TokenView tv){
        tv.setColorInGame(getPlayerColor());
        columnMoves[tv.getYindex()] = columnMoves[tv.getYindex()]+1;
        player = !player;
        System.out.println("Player: Xindex: "+tv.getXindex()+" | Yindex: "+tv.getYindex());
        aiboard[tv.getXindex()][tv.getYindex()] = 1; // el jugador le dio click a ese token
        refreshBoard();
    }
    
    private void runAIMove(TokenView tv){
        tv.setColorInGame(getAIColor());
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
        System.out.println(row+"|"+column+"----------------------------");
        aiboard[row][column] = 1;
        columnMoves[column] = columnMoves[column] + 1;
        System.out.println("Perfomed player move in: ("+row+","+column+")");
    }
    
    private void makeAIMove(int column){
        int row = (BOARD_HEIGHT-1)-columnMoves[column];
        System.out.println(row+"|"+column+"----------------------------");
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
        return columnMoves[column]+1 < BOARD_HEIGHT;
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
        
        if(tmpx==8)
            return 0;
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
        //double values[] = new double[8]; //las heuristicas acumuladas de los nodos hijos de cada una de las 8 opciones del
        // turno de la PC
        
        double hs[] = new double [8]; // la heuristica de la 1era capa de la PC 
        
        ArrayList<String> l_values = new ArrayList<>();
        ArrayList<String> l = new ArrayList<>();
        
        ArrayList<IDValue> ab_h = new ArrayList<>();
        ArrayList<IDValue> c_h = new ArrayList<>();
        
        ArrayList<IDValue> ab_max_h = new ArrayList<>();
        ArrayList<IDValue> c_max_h = new ArrayList<>();
        
        GNode master = new GNode("--", tk.getID(), 0, 0);
        
        for(int i = 0; i< BOARD_HEIGHT; i++){
            if(!isAIValidMove(i))
                continue;
            makeAIMove(i); System.out.println("*********************************");
            hs[i] = getHeuristicValue(i, true)+0.1;
            GNode child = new GNode(master.nodeID, ((BOARD_HEIGHT) - columnMoves[i])+""+i, 1, hs[i]);
            double alphabeta = Math.abs(alphabeta(DEFAULT_DEPTH, MAX, MIN, player, i, child));
            
            ab_h.add(new IDValue(((BOARD_HEIGHT) - columnMoves[i])+""+i, alphabeta));
            c_h.add(new IDValue(((BOARD_HEIGHT) - columnMoves[i])+""+i, hs[i]));
            
            if(hs[i]>alphabeta){
                ab_max_h.add(new IDValue(((BOARD_HEIGHT) - columnMoves[i])+""+i, alphabeta));
                c_max_h.add(new IDValue(((BOARD_HEIGHT) - columnMoves[i])+""+i, hs[i]));
            }
            
            master.addChild(child);
            undoAIMove(i); System.out.println("*********************************");
            //double tmp = alphabeta(2, -3, 3, player, i);
            System.out.println("=======================================");
            System.out.println("tmp:"+i+" "+alphabeta+" | value:"+val);
            System.out.println("=======================================");
        }
        
        System.out.println("Token maximixed id: "+x+y+"");
        
        try{
            if(mni_chk_graph.isSelected())
                GGraph.draw(master);
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, "Graph Error. Disable the option show graph");
            System.out.println("ERROR");
            e.printStackTrace();
            return hash_tokens.get(ab_h.get((Math.abs(new Random().nextInt())%ab_h.size())).position);
        }
        
        //Si ninguna heurisitica actual es mayor a la de alphabeta
        Collections.sort(c_max_h, new IDValueComparator());
        if(c_max_h.isEmpty()){
            int count = 1;
            for(int s = 0 ;s<ab_h.size()-1; s++){
                if(ab_h.get(s).value>ab_h.get(s+1).value){
                    break;
                }
                count++;
            }
            System.out.println("Returned the min value of alphabeta heuristics (Random Size)");
            return hash_tokens.get(ab_h.get(Math.abs(new Random().nextInt())%count).position);
        }
        
                
        for(int s = 0; s < c_max_h.size(); s++){
            IDValue idv = c_max_h.get(s);
            IDValue idv2 = ab_max_h.get(s);
            System.out.println("-> "+idv.value + " | "+idv.position +"--> "+idv2.value + " | "+idv.position );
        }
        
        //Ordenando de menor a mayor las heuriticas actuales
        Collections.sort(c_max_h, new IDValueComparator());
        
        for(int s = 0; s < c_max_h.size(); s++){
            IDValue idv = c_max_h.get(s);
            System.out.println("-> "+idv.value + " | "+idv.position);
        }
        
        //Arreglo que contendra las heuristicas alphabetas que hayan sido menores a las heuristicas actuales
        ArrayList<IDValue> final_mins = new ArrayList<>();
        //final_mins.add(c_max_h.get(c_max_h.size()-1));
        for(int t=c_max_h.size()-1; t>0; t-- ){
            //final_mins.add(c_max_h.get(t));
            if(c_max_h.get(t-1).value<c_max_h.get(t).value){
                IDValue removed = c_max_h.remove(t-1);
                t = t-1;
            //}else{
            //    final_mins.add(c_max_h.get(t));
            }
        }
            
         for(IDValue t: c_max_h){
             for(IDValue m: ab_max_h){
                 if(t.position.contentEquals(m.position))
                     final_mins.add(m);
             }
         }
        
        System.out.println(c_max_h.size()+"|"+final_mins.size());
        
        //Si solo hay una heuristica actual mayor a la de alphabeta
        if(c_max_h.size()==1){
            System.out.println("One element in current heuristic");
            return hash_tokens.get(c_max_h.get(0).position);
        }
        
        //Si hay mas heuristicas actuales mayores, pero iguales entre si, poda para retornar la minima de las heurisicas alphabeta (Random Size)
        
        if(c_max_h.size()>1){
            if(final_mins.size()==1){
                System.out.println("One element in alphabeta heuristic cutoff by h");
                return hash_tokens.get(final_mins.get(0).position);
            }else{
                Collections.sort(final_mins, new IDValueComparator());
                for(IDValue idv: final_mins)
                    System.out.println(idv.position+"|"+idv.value);
                int counter =1;
                for(int i=0; i< final_mins.size()-1; i++){
                    if(final_mins.get(i+1).value>final_mins.get(i).value){
                        System.out.println("(Random Size) Min element(s) of alphabeta");
                        return hash_tokens.get(final_mins.get((Math.abs(new Random().nextInt())%counter)).position);
                    }
                    counter++;
                }
                System.out.println("(Random Size) Random element in all mins of alphabeta");
                return hash_tokens.get(final_mins.get((Math.abs(new Random().nextInt())%final_mins.size())).position);
            }
        }
        
        JOptionPane.showMessageDialog(null, "Oops. My play not will be the best");
            
        return hash_tokens.get(ab_h.get((Math.abs(new Random().nextInt())%ab_h.size())).position);
        
    }
    
    
    double alphabeta(int depth, double alpha, double beta, boolean maximizingAI, int col, GNode parent) {
          //boolean hasWinner = board.hasWinner();
          
          // All these conditions lead to a
          // termination of the recursion
          if (depth == 0 ) {
              
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
                     
                     makeAIMove(column);
                     GNode child = new GNode(parent.nodeID, ((BOARD_HEIGHT-1) - columnMoves[column])+""+column, 2+(DEFAULT_DEPTH-depth), 0);
                     double newAlpha = Math.abs(alphabeta(depth-1, alpha, beta,false ,column,child));
                     child.hvalue = newAlpha;
                     parent.addChild(child);
                     
                     alpha = Math.max(alpha, (newAlpha));
                     currentAlpha = Math.max(currentAlpha,newAlpha);
                     
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
                     
                     makePlayerMove(column);
                     GNode child = new GNode(parent.nodeID, ((BOARD_HEIGHT-1) - columnMoves[column])+""+column, 2+(DEFAULT_DEPTH-depth), 0);
                     double newBeta = 0-alphabeta(depth-1,alpha,beta,true,column,child);
                     child.hvalue = newBeta;
                     parent.addChild(child);
                     
                     beta = Math.min(beta,newBeta);
                     currentBeta = Math.min(newBeta, currentBeta);
                     
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
            this.position = position;
            this.value = value;
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
        jPanel1 = new javax.swing.JPanel();
        pnl_general_info = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();
        pn_board = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lbl_player_wins = new javax.swing.JLabel();
        lbl_ai_wins = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        lbl_gif = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        mn_bar = new javax.swing.JMenuBar();
        mn_options = new javax.swing.JMenu();
        mni_new_game = new javax.swing.JMenuItem();
        mni_chk_graph = new javax.swing.JCheckBoxMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mni_exit = new javax.swing.JMenuItem();
        mn_about = new javax.swing.JMenu();
        mni_version = new javax.swing.JMenuItem();

        mni_help.setText("Ayuda");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Connect4 - IA Games");
        setMaximumSize(new java.awt.Dimension(430, 555));
        setMinimumSize(new java.awt.Dimension(430, 555));
        setResizable(false);

        javax.swing.GroupLayout pnl_general_infoLayout = new javax.swing.GroupLayout(pnl_general_info);
        pnl_general_info.setLayout(pnl_general_infoLayout);
        pnl_general_infoLayout.setHorizontalGroup(
            pnl_general_infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 193, Short.MAX_VALUE)
        );
        pnl_general_infoLayout.setVerticalGroup(
            pnl_general_infoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 72, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout pn_boardLayout = new javax.swing.GroupLayout(pn_board);
        pn_board.setLayout(pn_boardLayout);
        pn_boardLayout.setHorizontalGroup(
            pn_boardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        pn_boardLayout.setVerticalGroup(
            pn_boardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 412, Short.MAX_VALUE)
        );

        jLabel1.setBackground(new java.awt.Color(255, 255, 0));
        jLabel1.setText("Player wins:");

        jLabel2.setText("AI wins:");

        lbl_player_wins.setText("0");

        lbl_ai_wins.setText("0");

        jPanel3.setBackground(new java.awt.Color(255, 255, 0));
        jPanel3.setForeground(new java.awt.Color(255, 255, 0));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        jPanel6.setBackground(new java.awt.Color(255, 0, 0));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 13, Short.MAX_VALUE)
        );

        jLabel3.setText("Estadisticas");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lbl_player_wins, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel2)
                                .addGap(29, 29, 29)
                                .addComponent(lbl_ai_wins, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel3))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_gif, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lbl_gif, javax.swing.GroupLayout.DEFAULT_SIZE, 51, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(lbl_player_wins))
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel2)
                                .addComponent(lbl_ai_wins))
                            .addComponent(jPanel6, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        mn_options.setText("Opciones");

        mni_new_game.setText("Nuevo juego");
        mni_new_game.setActionCommand("mni_new_game");
        mni_new_game.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_new_gameActionPerformed(evt);
            }
        });
        mn_options.add(mni_new_game);

        mni_chk_graph.setText("Mostrar Grafo");
        mni_chk_graph.setName(""); // NOI18N
        mni_chk_graph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_chk_graphActionPerformed(evt);
            }
        });
        mn_options.add(mni_chk_graph);
        mn_options.add(jSeparator1);

        mni_exit.setText("Salir");
        mn_options.add(mni_exit);

        mn_bar.add(mn_options);

        mn_about.setText("Acerca de");

        mni_version.setText("Version");
        mni_version.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mni_versionActionPerformed(evt);
            }
        });
        mn_about.add(mni_version);

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
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pn_board, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 10, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(pnl_general_info, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl_general_info, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pn_board, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void mni_new_gameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_new_gameActionPerformed
        cleanBoard();
    }//GEN-LAST:event_mni_new_gameActionPerformed

    private void mni_chk_graphActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_chk_graphActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mni_chk_graphActionPerformed

    private void mni_versionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mni_versionActionPerformed
        JOptionPane.showMessageDialog(null,"Version 0.1","Connect4 Version",JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_mni_versionActionPerformed

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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel lbl_ai_wins;
    private javax.swing.JLabel lbl_gif;
    private javax.swing.JLabel lbl_player_wins;
    private javax.swing.JMenu mn_about;
    private javax.swing.JMenuBar mn_bar;
    private javax.swing.JMenu mn_options;
    private javax.swing.JCheckBoxMenuItem mni_chk_graph;
    private javax.swing.JMenuItem mni_exit;
    private javax.swing.JMenuItem mni_help;
    private javax.swing.JMenuItem mni_new_game;
    private javax.swing.JMenuItem mni_version;
    private javax.swing.JPanel pn_board;
    private javax.swing.JPanel pnl_general_info;
    // End of variables declaration//GEN-END:variables
}
