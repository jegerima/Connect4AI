/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import static connect4.Connect4.BOARD_WIDTH;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 *
 * @author Jefferson
 */
public class MainBoard extends javax.swing.JFrame {

    private GridLayout BOARD;
    private boolean player = false;
    private boolean playerYellow = true;
    private HashMap<String,TokenView> hash_tokens;
    private String[][] matrix;
    private int[][] aiboard;
    private int columnMoves[];
    
    public static int BOARD_WIDTH = 8;
    public static int BOARD_HEIGHT = 8;
    
    static final int MAX_DEPTH = 8;
    
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
            columnMoves[i] = 0;
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
                        
                        //if(player)
                            runPlayerMove(token);
                            runAIMove(testAIMove());
                            
                            
                        //else
//                            runAIMove(token);
                        /*
                        Color tmp = playerYellow ? Color.YELLOW : Color.RED;
                        token.setColorInGame(tmp);
                        //System.out.println("clicked");
                        player = !player;
                        System.out.println(token.getID());
                        refreshBoard();
                        */
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
    
    
    
    //Ai Board Functions
    // Player = 1
    // AI = -1
    
    private void runPlayerMove(TokenView tv){
        //TokenView tv = hash_tokens.get(xy);
        tv.setColorInGame(getPlayerColor());
        System.out.println("Final Heuristic Player: "+getHeuristicValue(tv.getYindex(), player));
        player = !player;
        System.out.println("Player: Xindex: "+tv.getXindex()+" | Yindex: "+tv.getYindex());
        aiboard[tv.getXindex()][tv.getYindex()] = 1;
        columnMoves[tv.getYindex()] = columnMoves[tv.getYindex()]+1;
        refreshBoard();
    }
    
    private void runAIMove(TokenView tv){
        tv.setColorInGame(getAIColor());
        System.out.println("AI: Final Heuristic AI: "+getHeuristicValue(tv.getYindex(), player));
        player = !player;
        System.out.println("Xindex: "+tv.getXindex()+" | Yindex: "+tv.getYindex());
        aiboard[tv.getXindex()][tv.getYindex()] = -1;
        columnMoves[tv.getYindex()] = columnMoves[tv.getYindex()]+1;
        refreshBoard();
    }
    
    //**************************************************
    
    private TokenView testAIMove(){
        int x = 0;
        int y = 0;
        double val = 0.0;
        for(int i = 0; i< BOARD_HEIGHT; i++){
            double tmp = alphabeta(4, Integer.MAX_VALUE, Integer.MIN_VALUE, player, i);
            if(tmp>val){
                x = (BOARD_HEIGHT-1) - columnMoves[i];
                y = i;
            }
        }
        System.out.println("Token maximixed id: "+x+y+"");
        return hash_tokens.get(""+x+y+"");
    }
    
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
        int row = (BOARD_HEIGHT-1)-columnMoves[column];
        aiboard[row][column] = 0;
        columnMoves[column] = columnMoves[column] - 1;
        System.out.println("Undo player move in: ("+row+","+column+")");
    }
    
    private void undoAIMove(int column){
        int row = (BOARD_HEIGHT-1)-columnMoves[column];
        aiboard[row][column] = 0;
        columnMoves[column] = columnMoves[column] - 1;
        System.out.println("Undo AI move in: ("+row+","+column+")");
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
    
    int getHeuristicValue(int column, boolean player){
        int val = player?1:-1;
        int x = (BOARD_HEIGHT-1)-columnMoves[column];    //Row
        int y = column;                 //Column
        
        System.out.println("Position: ("+x+","+y+")");
        
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
        
        //Right
        if(y<(BOARD_WIDTH-1)){
            boolean pro = false;    //A favor
            boolean against = false;
            for(int i=y; i<y+3;i++){
                if(i>=(BOARD_WIDTH-1))break;
                                
                if(aiboard[x][i+1]==0){        //Nobody has played that position
                    break;
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
                    break;
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
                    break;
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
        
        System.out.println("Horizontal: "+(hlv_p+hrv_p)+"|"+(hlv_n+hrv_n));
//        System.out.println("Vertical:"+vuv_p+"+"+vdv_p+"|"+vuv_n+"+"+vdv_n);
        System.out.println("Vertical: "+vdv_p+"|"+vdv_n);
        System.out.println("Diag. princ: "+(nev_p+swv_p)+"|"+(nev_n+swv_n));
        System.out.println("Otra diagonal: "+(sev_p+nwv_p)+"|"+(sev_n+nwv_n));      
        
        int max = Math.max((hlv_p+hrv_p), (vuv_p+vdv_p));
        //System.out.println("Max1: "+max);
        max = Math.max(max, (nev_p+swv_p));
        //System.out.println("Max2: "+max);
        max = Math.max(max, (sev_p+nwv_p));
        
        int min = Math.min((hlv_n+hrv_n),(vuv_n+vdv_n));
        //System.out.println("Min1: "+min);
        min = Math.min(min,(nev_n+swv_n));
        //System.out.println("Min2: "+min);
        min = Math.min(min,(sev_n+nwv_n));
        
        System.out.println("MaxF: "+max);
        System.out.println("MinF: "+min);
                
        if(Math.abs(max)>Math.abs(min))
            return max;
        if(Math.abs(max)<Math.abs(min))
            return min;
        
        return (new Random()).nextBoolean()? max:min;
    }
    
    
    
    double alphabeta(int depth, double alpha, double beta, boolean maximizingPlayer, int col) {
          //boolean hasWinner = board.hasWinner();
          boolean hasWinner = false;
          
          // All these conditions lead to a
          // termination of the recursion
          if (depth == 0 || hasWinner) {
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
              return getHeuristicValue(col, player);
         }

         if (maximizingPlayer) {
             for (int column = 0; column < BOARD_WIDTH; column++) {
                 if (isAIValidMove(column)) {
                     makeAIMove(column);
                     alpha = Math.max(alpha, alphabeta(depth-1, alpha, beta, false,col));
                     //alpha = Math.max(alpha, alphabeta(depth – 1, alpha, beta,false));
                     undoAIMove(column);
                     if (beta <= alpha) {
                         break;
                     }
                 }
             }
             return alpha;
         } else {
             for (int column = 0; column < BOARD_WIDTH; column++) {
                 if (isAIValidMove(column)) {
                     makePlayerMove(column);
                     beta = Math.min(beta,alphabeta(depth-1,alpha,beta,true,col));
                     //beta = Math.min(beta,alphabeta(depth – 1,alpha, beta, true));
                     undoPlayerMove(column);
                     if (beta <= alpha) {
                         break;
                     }
                 }
             }
             return beta;
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
