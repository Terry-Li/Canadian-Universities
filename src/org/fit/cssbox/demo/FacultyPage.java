/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

/**
 *
 * @author Terry
 */
public class FacultyPage extends Thread{
    public String url;
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

    public FacultyPage(String url) {
        this.url = url;
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
    
    public static void getTextNodes(Element root, ArrayList<String> texts) {
        List<TextNode> nodes = root.textNodes();
        for (TextNode node: nodes) {
            texts.add(node.text());
        }
        for (Element child: root.children()) {
            getTextNodes(child, texts);
        }
    }
    
    public static FacultyScore computeScore(String url) throws IOException {
        FacultyScore score = new FacultyScore();
        Element body = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get().body();
        ArrayList<String> texts = new ArrayList<String>();
        getTextNodes(body, texts);
        for (String text: texts) {
            if (isName(text)) score.name++;
            if (text.toLowerCase().contains("professor")) score.professor++;
            if (text.toLowerCase().contains("@")) score.email++;
            if (text.matches(".*\\d{3}.*")) score.phone++;
        }
        return score;
    }
    
    public static void identify(String url) throws IOException {
        FacultyScore score = computeScore(url);
        score.total = score.name+score.professor+score.email+score.phone;
        sb.append(url+" ("+score.name+", "+score.professor+", "+score.email+", "+score.phone+", "+score.total+")\n");
    }
    
    public void run() {
        try {
            identify(url);
        } catch (IOException ex) {
            Logger.getLogger(FacultyPage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void main(String[] args) throws IOException, BiffException, InterruptedException {
        //Workbook workbook = Workbook.getWorkbook(new File("faculty200.xls"));
        //Sheet sheet = workbook.getSheet(3);
        //FacultyPage[] threads = new FacultyPage[sheet.getRows()];
        ArrayList<String> keywords = Utility.getKeywords("FacultyListUrl.txt");
        FacultyPage[] threads = new FacultyPage[keywords.size()];
        for (int i = 0; i < threads.length; i++) {
            //String url = sheet.getCell(0, i).getContents().trim();
            threads[i] = new FacultyPage(keywords.get(i));
            threads[i].start();
        }
        for (int j = 0; j < threads.length; j++) {
            threads[j].join();
        }
        FileUtils.writeStringToFile(new File("FacultyScore.txt"), sb.toString());
    }
}
