/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Terry
 */
public class Resource {

    /**
     * @param args the command line arguments
     */
    
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
    
    public static void getInsertWithPhoto() throws IOException{
        File[] inserts = new File("Insert").listFiles();
        File[] photos = new File("photo").listFiles();
        for (File insert: inserts) {
            for (File photo: photos) {
                if (photo.getName().equals(insert.getName().split("\\.")[0])) {
                    FileUtils.copyFile(insert, new File("Insert_0430/"+insert.getName()));
                    break;
                }
            }
        }
    }
    
    public static void getInsertWithBigSize() throws FileNotFoundException, IOException {
        ArrayList<String> source = getKeywords("UnivDirectory.txt"); 
        File[] files = new File("Insert").listFiles();
        StringBuilder sb = new StringBuilder();
        for (File file: files) {
            if (file.length() < 100) {
                //FileUtils.copyFile(file, new File("Insert_Small/"+file.getName()));
                for (String univ: source) {
                    if (univ.split("==")[0].equals(file.getName().split("\\.")[0])) {
                        sb.append(univ+"\n");
                    }
                }
            } 
        }
        FileUtils.writeStringToFile(new File("WeirdDeals.txt"), sb.toString());
    }
    
    public static void getInsertNull() throws IOException{
        StringBuilder sb = new StringBuilder();
        File[] inserts = new File("SchoolNav").listFiles();
        for (File insert: inserts) {
            ArrayList<String> rows = getKeywords("SchoolNav/"+insert.getName());
            if (rows.size()>=2) {
                String[] tokens = rows.get(1).split("==");
                if (tokens.length==2 && tokens[1].equals("null")) {
                    sb.append(rows.get(0)+"\n");
                }
            }
        }
        FileUtils.writeStringToFile(new File("SchoolNulls.txt"), sb.toString());
    }
    
    
    public static void main(String[] args) throws IOException {
        getInsertNull();
    }
}
