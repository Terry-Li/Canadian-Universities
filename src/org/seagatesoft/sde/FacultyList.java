/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seagatesoft.sde;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    
    public static String[] getLink(String cell){
        String[] pair = new String[2];
        Pattern p = Pattern.compile("href=\"(.*)\".*</a>(.*)", Pattern.DOTALL);
        Matcher match = p.matcher(cell);
        if (match.find()) {
            pair[0] = match.group(2);
            pair[1] = match.group(1);
        }
        return pair;
    }
    
    
    public static boolean nameColumn(String[][] table, int col){
        int names = 0;
        int rows = table.length;
        Set<String> keywords = new HashSet<String>();
        for (int j = 0; j < rows; j++) {
            if (containNames(table[j][col], keywords) && !table[j][col].toLowerCase().contains(" at ")&& !table[j][col].toLowerCase().contains("@")) {
                names++;
            }
        }
        if (keywords.size() >= (float) rows * 0.5 && names >= (float) rows * 0.5) {
            return true;
        } else return false;
    }
    
    public static boolean webColumn(String[][] table, int col) {
        int count = 0;
        for (int j = 0; j < table.length; j++) {
            String current = table[j][col];
            if (current!=null && current.contains("Link&lt;&lt;") && !current.contains("@")) {
                count++;
            }

        }
        return count*2 >= table.length;
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
    
    public static boolean emailColumn(String[][] table, int col){
        for (int row = 0; row < table.length; row++) {
            String current = table[row][col];
            if (current != null && current.contains("@")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean phoneColumn(String[][] table, int col){
        for (int row = 0; row < table.length; row++) {
            String current = table[row][col];
            if (current != null && current.replaceAll(" ", "").matches(".*[\\d\\-\\.\\+\\(\\)]{8,}.*")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean positionColumn(String[][] table, int col){
        for (int row = 0; row < table.length; row++) {
            String current = table[row][col];
            if (current != null && current.toLowerCase().contains("professor")) {
                return true;
            }
        }
        return false;
    }
    
    public static void main(String[] args) {
        String input = "David Avis, Office: MC308, Phone: +1-514-398-3737, Email: avis@cs.mcgill.ca";
        System.out.println(input.matches(".*[\\d\\-\\.\\+\\(\\)]{8,}.*"));
        Pattern p = Pattern.compile("[\\d\\-\\.\\+\\(\\)]{8,}");
        Matcher m = p.matcher(input.replaceAll(" ", ""));
        if (m.find()) {
            System.out.println(m.group());
        }
    }
       
    public static void addPhoto(String[][] result, String[][] temp, int col){
        for (int i=0; i<temp.length; i++) {
            result[i][0] = temp[i][col];
        }
    }
    
    public static void addName(String[][] result, String[][] temp, int col){
        for (int i=0; i<temp.length; i++) {
            if (temp[i][col] != null) {
                result[i][1] = temp[i][col].replaceAll("<a.*a>","").trim();
            }
        }
    }
    
    public static void addWeb(String[][] result, String[][] temp, int col){
        for (int i=0; i<temp.length; i++) {
            if (temp[i][col] != null) {
                result[i][2] = getLink(temp[i][col])[1];
            }
        }
    }
    
    public static void addPosition(String[][] result, String[][] temp, int col){
        for (int i=0; i<temp.length; i++) {
            result[i][3] = temp[i][col];
        }
    }
    
    public static void addEmail(String[][] result, String[][] temp, int col){
        for (int i=0; i<temp.length; i++) {
            if (temp[i][col] != null) {
                if (temp[i][col].contains("Link&lt;&lt;")) {
                    result[i][4] = getLink(temp[i][col])[1];
                } else {
                    result[i][4] = temp[i][col];
                }
            }
        }
    }
    
    public static void addPhone(String[][] result, String[][] temp, int col){
        Pattern p = Pattern.compile("[\\d\\-\\.\\+\\(\\)]{8,}");
        for (int i=0; i<temp.length; i++) {
            if (temp[i][col] != null) {
                Matcher m = p.matcher(temp[i][col].replaceAll(" ", ""));
                if (m.find()) {
                    result[i][5] = m.group();
                }
            }
        }
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
