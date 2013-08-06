/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seagatesoft.sde;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Yifeng
 */
public class FacultyList {
    public static List<String> keywords;
    static {
        try {
            keywords = FileUtils.readLines(new File("Group/Names.txt"));
        } catch (IOException ex) {
            Logger.getLogger(FacultyList.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static boolean containNames(String text, Set<String> temps) {
        //System.out.println(text);
        if (text==null || text.matches(".*\\d.*")) return false;
        text = text.replaceAll("<a.*a>","").trim().replaceAll("\\W+", " ");
        String[] tokens = text.split(" ");;
        if (tokens.length > 6) return false;
        //System.out.println(Arrays.toString(tokens));
        for (String keyword: keywords) {
            for (String token: tokens) {
                String name = token.trim().replaceAll(",", "");
                if (keyword.trim().equalsIgnoreCase(name)) {
                    //System.out.println(text+"("+keyword.trim()+")");
                    temps.add(name);
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
            if (containNames(table[j][col], keywords) && !table[j][col].toLowerCase().contains(" at ") && !table[j][col].toLowerCase().contains("@")) {
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
    
    public static boolean nameColumn2(String[][] table, int col){
        boolean hasName = false;
        int names = 0;
        int rows = table.length;
        Set<String> keywords = new HashSet<String>();
        for (int j = 0; j < rows; j++) {
            if (containNames(table[j][col], keywords) && !table[j][col].toLowerCase().contains(" at ")&& !table[j][col].toLowerCase().contains("@")) {
                names++;
            }
        }
        //if (rows==30)System.out.println(names+"/"+rows);
        if (!hasName && keywords.size() >= (float) rows * 0.5 && names >= (float) rows * 0.5) {//&& keywords.size()>=(float)rows*0.5
            hasName = true;
        }
        return hasName;
    }
    
    public static boolean webColumn(String[][] table, int col) {
        int count = 0;
        Set<String> anchors = new HashSet<String>();
        for (int j = 0; j < table.length; j++) {
            String current = table[j][col];
            if (current!=null && (!current.contains("Link&lt;&lt;") || current.contains("@"))) {
                return false;
            }
            if (current!=null) {
                if (current.split("a>").length == 2) {
                    anchors.add(current.split("a>")[1].trim());
                }
                count++;
            }
        }
        //System.out.println(count+":"+table.length+":"+anchors.size());
        if (count*2>table.length &&  anchors.size()<=2) {
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
    
    public static boolean identify(String[][] table, boolean intermediateResult){
        if (intermediateResult) return true;
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
