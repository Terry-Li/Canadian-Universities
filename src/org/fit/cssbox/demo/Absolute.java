/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Terry
 */
public class Absolute {
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        File[] files = new File("Univs").listFiles();
        for (File f: files) {
            ArrayList<String> schools = Utility.getKeywords("Univs//"+f.getName());
            String url = schools.get(1).split("==")[1];
            //System.out.println(url);
            if (url.equals("null") || url.contains("http")) continue;
            //System.out.println("----------------------");
            StringBuilder sb = new StringBuilder();
            sb.append(schools.get(0)+"\n");
            URL base = new URL(schools.get(0));
            for (int i=1;i<schools.size();i++) {
                String[] tokens = schools.get(i).split("==");
                sb.append(tokens[0]+"=="+new URL(base,tokens[1]).toString()+"\n");
            }
            FileUtils.writeStringToFile(new File("Univs//"+f.getName()+"s.txt"), sb.toString());
        }
    }
}
