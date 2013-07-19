/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Terry
 */
public class CSSModel {
     /**
     * Recursively prints the text boxes from the specified tree
     */
    static String title = "";
    private static void printTextBoxes(Box root, ArrayList<Combo> combos, URL base) throws MalformedURLException
    {   
        if (root instanceof TextBox)
        {   
            TextBox text = (TextBox) root;
            //System.out.println(text.getText());
            if (combos.size()>0 && text.getParent().getElement()==combos.get(combos.size()-1).dom) {
                
                combos.get(combos.size()-1).text = combos.get(combos.size()-1).text + " " + text.getText();
                combos.get(combos.size()-1).contract = combos.get(combos.size()-1).contract + text.getContentHeight();
                //System.out.println(combos.get(combos.size()-1).contract);
            } else {
                Combo c = new Combo(text.getAbsoluteBounds().x, text.getAbsoluteBounds().y, text.getText(), text.getParent().getElement().getTagName(), text.getParent().getElement());
                if (text.getParent() != null && text.getParent().getElement().getTagName().equals("a")) {
                    c.setUrl(text.getParent().getElement().getAttribute("href"));
                } else if (text.getParent().getParent() != null && text.getParent().getParent().getElement().getTagName().equals("a")) {
                    c.setUrl(text.getParent().getParent().getElement().getAttribute("href"));
                } else if (text.getParent().getParent().getParent() != null && text.getParent().getParent().getParent().getElement().getTagName().equals("a")) {
                    c.setUrl(text.getParent().getParent().getParent().getElement().getAttribute("href"));
                }
                //if (c.url!=null && !c.url.contains("http")) c.setUrl(new URL(base,c.url).toString());
                c.setHeight(text.getContentHeight());
                c.style = text.getParent().getStyleString();
                c.title = title;
                c.setParent(text.getParent().getParent().getElement().getTagName());
                if (c.parent.equals("Xdiv") && text.getParent().getParent().getParent() != null) {
                    c.setParent(text.getParent().getParent().getParent().getElement().getTagName());
                }
                if (!c.text.trim().equals(" ")) {
                    combos.add(c);
                }
            }
        }
        else if (root instanceof ElementBox)
        {
            //element boxes must be just traversed
            ElementBox el = (ElementBox) root;
            String tag = el.getElement().getTagName();
            if (tag.equalsIgnoreCase("h1") || tag.equalsIgnoreCase("h2") || tag.equalsIgnoreCase("h3") || 
                    tag.equalsIgnoreCase("h4") || tag.equalsIgnoreCase("h5")) {
                title = el.getText();
            }
            if (tag.equalsIgnoreCase("img") && el.getParent() != null) {
                Element parent = el.getParent().getElement();
                if (parent.getTagName().equalsIgnoreCase("a")) {
                    Combo c = new Combo();
                    c.x = el.getAbsoluteContentX();
                    c.y = el.getAbsoluteContentY();
                                    
                    c.text = parent.getAttribute("title");//el.getElement().getAttribute("alt");
                    //System.out.println("Inspecting:  "+c.text);
                    if (c.text == null || c.text.equals("")) c.text = el.getElement().getAttribute("alt");
                    c.url = parent.getAttribute("href");
                    c.height = el.getContentHeight();
                    c.style = el.getStyleString();
                    c.title = "";
                    combos.add(c);
                    //System.out.println(alt+": \n"+c.style+"\nheight: "+c.height+"\nY: "+c.y);
                }
            }
            for (int i = el.getStartChild(); i < el.getEndChild(); i++)
                printTextBoxes(el.getSubBox(i), combos, base);
        }
    }
    
    /**
     * main method
     */
    public static ArrayList<Combo> getCombos(String url) throws MalformedURLException
    {   
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Getting combos from: "+url+"...");
        ArrayList<Combo> combos = new ArrayList<Combo>();
        ArrayList<String> tags = new ArrayList<String>();
        try {
            //Open the network connection 
            DocumentSource docSource = new DefaultDocumentSource(url);
            
            //Parse the input document
            DOMSource parser = new DefaultDOMSource(docSource);
            Document doc = parser.parse();
            
            //Create the CSS analyzer
            DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
            da.getStyleSheets(); //load the author style sheets
            
            //Create the browser canvas
            BrowserCanvas browser = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
            //Disable the image loading
            browser.getConfig().setLoadImages(false);
            browser.getConfig().setLoadBackgroundImages(false);
            
            //Create the layout for 1000x600 pixels
            browser.createLayout(new java.awt.Dimension(1000, 600));
            
            //Display the result
            URL base = new URL(url);
            printTextBoxes(browser.getViewport(), combos, base);
            
            docSource.close();
            
        } catch (Exception e) {
            System.err.println("Error: "+e.getMessage());
            e.printStackTrace();
            return new ArrayList<Combo>();
        } 
        
        for (Iterator<Combo> it = combos.iterator(); it.hasNext();) {
            Combo c = it.next();
            //System.out.println(c);
            if (c.text.equals("") ||
                c.text.split(" ").length>23 || !Utility.isDomainLink(c.url)) { 
                //System.out.println(c);
                it.remove();
            } 
        }
        /*
        for (Combo c: combos) {
            if (c.tag.equalsIgnoreCase("strong") && c.parent.equalsIgnoreCase("a")) {
                c.tag = "a";
                c.parent = "strong";
            }
            if (c.tag.equalsIgnoreCase("font") && c.parent.equalsIgnoreCase("font")) {
                c.parent = "a";
            }
        } */
        for (int i=1;i<combos.size();i++) {
            combos.get(i).previous = combos.get(i-1);
        }
        return combos;

    }
    
}
