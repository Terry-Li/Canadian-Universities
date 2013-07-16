/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Terry
 */
public class Comapre {
    public static void compare(HashMap<String,ArrayList<String>> extracted, HashMap<String,ArrayList<String>> golden) {
        //System.out.println(extracted.size()+"/"+golden.size());
        int count = 0;
        for (String key: extracted.keySet()) {
            int extractedSize = extracted.get(key).size();
            ArrayList<String> goldenList = golden.get(key);
            if (goldenList!=null) {
                int goldenSize = goldenList.size();
                if (extractedSize == goldenSize) {
                    count++;
                }
            }
        }
        System.out.println(count+"/"+extracted.size());
    }
    
    public static void countUrl(HashMap<String,ArrayList<String>> extracted) {
        int count = 0;
        for (String key: extracted.keySet()) {
            String url = extracted.get(key).get(0).split("==")[1];
            if (url.equals("null")) count++;
        }
        System.out.println(count);
    }
    
    public static void main(String[] args) throws IOException {
        String results = FileUtils.readFileToString(new File("results.txt"));
        String[] univs = results.split("\n\n");
        HashMap<String,ArrayList<String>> extracted = new HashMap<String,ArrayList<String>>();
        for (String univ: univs) {
            String[] schools = univ.split("\n");
            if (schools.length != 1) {
                String key = schools[0];
                ArrayList<String> value = new ArrayList<String>();
                for (int i=1;i<schools.length;i++) {
                    value.add(schools[i]);                   
                }
                extracted.put(key, value);
            }
        }
        
        
        String manuals = FileUtils.readFileToString(new File("100-Schools.txt"));
        String[] univs2 = manuals.split("\n\n");
        HashMap<String,ArrayList<String>> golden = new HashMap<String,ArrayList<String>>();
        for (String univ: univs2) {
            String[] schools = univ.split("\n");
            if (schools.length != 1) {
                String key = schools[0];
                ArrayList<String> value = new ArrayList<String>();
                for (int i=1;i<schools.length;i++) {
                    value.add(schools[i]);                   
                }
                golden.put(key, value);
            }
        }
        
        compare(extracted,golden);
        countUrl(extracted);
    }
}
