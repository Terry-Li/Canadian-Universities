/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seagatesoft.sde;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Yifeng
 */
public class FacultyList {
    public static ArrayList<String> keywords;
    static {
        try {
            keywords = getKeywords("Names.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FacultyList.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FacultyList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ArrayList<String> getKeywords(String file) throws FileNotFoundException, IOException {
        ArrayList<String> keywords = new ArrayList<String>();
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null && !strLine.equals("")) {
            keywords.add(strLine);
        }
        return keywords;
    }
    
    public static boolean containNames(String text, Set<String> temps) {
        //System.out.println(text);
        if (text==null || text.trim().replaceAll(" +", " ").split(" ").length > 6) return false;
        String[] tokens;
        text = text.replaceAll("<a.*a>","").trim().replaceAll(" +", " ");
        if (text.contains(",")) {
            tokens = text.split(", ");
        } else {
            tokens = text.split(" ");
        }
        //System.out.println(Arrays.toString(tokens));
        for (String keyword: keywords) {
            for (String token: tokens) {
                if (keyword.trim().equalsIgnoreCase(token.trim())) {
                    //System.out.println(text+"("+keyword.trim()+")");
                    temps.add(keyword.trim());
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean nameColumn(String[][] table, int col){
        boolean hasLink = false;
        boolean hasName = false;
        int names = 0;
        int rows = table.length;
        Set<String> keywords = new HashSet<String>();
        for (int j = 0; j < rows; j++) {
            if (containNames(table[j][col], keywords) && !table[j][col].toLowerCase().contains(" at ")) {
                names++;
            }
            if (!hasLink && table[j][col] != null && table[j][col].contains("Link&lt;&lt;")) {
                hasLink = true;
            }
        }
        //if (rows==30)System.out.println(names+"/"+rows);
        if (!hasName && keywords.size() >= (float) rows * 0.5 && names >= (float) rows * 0.5) {//&& keywords.size()>=(float)rows*0.5
            hasName = true;
        }
        if (hasLink && hasName) {
            return true;
        }
        return false;
    }
    
    public static boolean photoColumn(String[][] table, int col){
        for (int row = 0; row < table.length; row++) {
            String current = table[row][col];
            if (current != null && current.startsWith("<img")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean identify(String[][] table){
        //if (true)return true;
        boolean hasLink = false;
        boolean hasName = false;
        int rows = table.length;
        int cols = table[0].length;
        for (int i=0;i<cols;i++){
            int names = 0;
            Set<String> keywords = new HashSet<String>();
            for (int j=0;j<rows;j++) {
                if (containNames(table[j][i], keywords)){
                    names++;
                } 
                if (!hasLink && table[j][i]!=null &&table[j][i].contains("Link&lt;&lt;")) {
                    hasLink = true;
                }
            }
            //if (rows==30)System.out.println(names+"/"+rows);
            if (!hasName && keywords.size()>=(float)rows*0.5 && names >= (float)rows*0.66) {//&& keywords.size()>=(float)rows*0.5
                hasName = true;
            }
            if (hasLink && hasName) return true;
        }
        return false;
    }
}
