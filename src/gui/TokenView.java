/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;

/**
 *
 * @author Jefferson
 */

public class TokenView extends JComponent{
    private Rectangle base = new Rectangle();
    private String status = "empty";
    private DrawCircle circle = new DrawCircle();
    private Color currentColor;
    private Color initialColor;
    private Graphics2D g2d;
    private boolean locked = false;
    private int x = -1;
    private int y = -1;
    private String id = "";
    private boolean isHint = false;
    private boolean isHover = false;
    private int d = 5;
    
    public TokenView(Color color, int x, int y){
        this.currentColor = color;
        this.initialColor = color;
        this.x = x;
        this.y = y;
        this.id = Integer.toString(x)+Integer.toString(y);
    }
    
    public String getID(){
        return this.id;
    }
    
    public void reInit(){
        this.locked = false;
        this.isHint = false;
        this.isHover = false;
        this.currentColor = Color.WHITE;
        //g2d.fillOval(d/2, d/2, getWidth()-d, getHeight()-d);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        if(isHint){
            g2d.setColor(Color.WHITE);
            g2d.fillOval(d/2, d/2, getWidth()-d, getHeight()-d);
            g2d.setColor(this.currentColor);
            if(isHover){
                g2d.setColor(this.currentColor);
                g2d.fillOval(d/2, d/2, getWidth()-d, getHeight()-d);
            }else{
                Stroke oldSroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(4));
                g2d.drawOval(d/2, d/2, getWidth()-d, getHeight()-d);
                g2d.setStroke(oldSroke);
            }
        }else{
            g2d.setColor(this.currentColor);
            g2d.fillOval(d/2, d/2, getWidth()-d, getHeight()-d);
        }
        
    }
    
    public int getXindex(){
        return this.x;
    }
    
    public int getYindex(){
        return this.y;
    }
    
    public boolean isLocked(){
        return this.locked;
    }
    
    public boolean isHint(){
        return this.isHint;
    }
    
    public void setLikeHint(){
        this.isHint = true;
        this.currentColor = Color.GRAY;
        repaint();
    }
    
    public void setHover(){
        this.isHover = true;
        this.currentColor = Color.GRAY;
        repaint();
    }
    
    public void deleteHover(){
        this.isHover = false;
        this.currentColor = Color.GRAY;
        repaint();
    }
    
    public void setColorInGame(Color color){
        if(locked){
            return;
        }
        this.currentColor = color;
        this.locked = true;
        this.isHint = false;
        repaint();
    }
    
    

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(50, 50);
    }

    
    class DrawCircle{
        public void draw(Graphics2D g2d, int w, int h, Color color) {
            g2d.setColor(color);
            g2d.fillOval(10, 10, w / 2, h / 2);
        }
    }
}
