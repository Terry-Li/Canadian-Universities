/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yifeng
 */
public class VisualFaculty {
    public static ArrayList<String> names;
    static {
        try {
            names = Utility.getKeywords("Group/Names.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VisualFaculty.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VisualFaculty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //public static ArrayList<String> getNames(ArrayList<Combo> combos){
        
    //}
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        ListEngine facultyEngine = new ListEngine(names, new ArrayList<String>(),new ArrayList<String>());
        ArrayList<Combo> combos = CSSModel.getCombos("http://www-cs.stanford.edu/faculty");
        for (Combo c: combos) {
            System.out.println(c.text+" -> "+c.x);
        }
        ArrayList<String> rows = facultyEngine.getNames("http://www-cs.stanford.edu/faculty", combos);
        for (int i=0;i<rows.size();i++) {
            //System.out.println(rows.get(i));
        }
    }
}
