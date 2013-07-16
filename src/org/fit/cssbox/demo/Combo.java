/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;


import java.awt.Color;
import org.w3c.dom.Element;

/**
 *
 * @author Terry
 */
public class Combo {
    public int index;
    public int x;
    public int y;
    public String text;
    public String tag;
    public String url;
    public int group;
    public int height;
    public double font;
    public String parent;
    public Element dom;
    public int contract;
    public Combo previous;
    public String style;
    public String title;

    public Combo(int x, int y, String text, String tag, Element dom) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.tag = tag;
        this.dom = dom;
    }

    public Combo() {
         
    }
    
    
    
    public void setParent(String parent) {
        this.parent = parent;
    }
    
    public void setFont(double font) {
        this.font = font;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public void setContract(int contract) {
        this.contract = contract;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void setGroup(int group) {
        this.group = group;
    }
    
    public String toString() {
        return "x="+x+" y="+y+" text="+text+" height="+height+" contract="+contract+" group="+group;//+" style="+style;
    }
    
}
