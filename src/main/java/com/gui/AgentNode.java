package com.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class AgentNode {
    private double x;
    private double y;
    private double height;
    private double width;
    public String text;
    private Color col;

    public AgentNode(double x, double y, double width, double height, String text, Color col){
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.text = text;
        this.col = col;
    }

    public void draw(GraphicsContext gc){
        gc.setFill(col);
        gc.fillRect(x - width/2, y - height/2, width, height);
        gc.setFill(Color.WHITE);
        gc.fillText(text, x, y);
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }

    public double getHeight(){
        return height;
    }

    public double getWidth(){
        return width;
    }
}
