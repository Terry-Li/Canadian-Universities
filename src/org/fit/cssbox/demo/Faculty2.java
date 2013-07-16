/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;



import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 *
 * @author Terry
 */
public class Faculty2 extends Thread{
    String url;
    public static int counter;
    public static ArrayList<String> keywords;
    public static StringBuffer sb = new StringBuffer();
    static {
        try {
            keywords = Utility.getKeywords("Names.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    public Faculty2(String url) {
        this.url = url;
    }

    public synchronized static void increment() {
        counter++;
    }
    
    public static boolean isName(String text) {
        String[] names = text.toUpperCase().trim().split(" ");
        if (names.length <= 4) {
            for (String name : names) {
                for (String keyword : keywords) {
                    if (name.equals(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

  
    public static boolean valid(ArrayList<Combo> combos) {
        int count = 0;
        int size = combos.size();
        for (Combo c: combos) {
            
            if (isName(c.text) || c.text.contains("@") || c.text.contains("Professor") || c.text.contains("Ph.D.")) {
                count++;
                //System.out.println(c.text);
            }
        }
        //System.out.println("------------------------");
        if (size > 3 && count > size*2/3) {return true;}
        else return false;
    }
    
    
    public static boolean identify(String link) throws MalformedURLException {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Combo> combos = CSSModel.getCombos(link);
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.tag + "" + c.height + "" + c.parent;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        for (String key: sets.keySet()) {
            ArrayList<Combo> value = sets.get(key);
            if (valid(value)) {
                for (Combo c: value) {
                    //System.out.println(c.text);
                    names.add(c.text+"=="+c.url);
                }
            }
        }
        if (names.size() > 4) {
            return true;
        } else return false;
    }
    
}
