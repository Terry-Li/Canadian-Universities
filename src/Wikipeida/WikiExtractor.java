/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wikipeida;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author lyf
 */
public class WikiExtractor {
    public static String path = "../Input/";
    
    public static void extractTable(String file) throws IOException{
        String html = FileUtils.readFileToString(new File(file));
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table");
        String motto = "---------------------";
        Element attrTable = attrTable(tables);
        if (attrTable != null) {
            Elements trs = attrTable.select("tr");
            for (Element tr : trs) {
                String th = tr.select("th").text();
                String td = tr.select("td").text();
                if (th.equals("Motto in English")) {
                    motto = td;
                    break;
                }
            }
        }
        print(motto);
    }
    
    public static void print(String text){
        System.out.println(text);
    }
    
    public static void delay(int seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(WikiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Element attrTable(Elements tables) {
        for (Element table : tables) {
            Elements ths = table.select("th");
            for (Element th : ths) {
                if (th.text().contains("Motto")) {
                    return table;
                }
            }
        }
        return null;
    }
    
    public static void main(String[] args) {
        List<String> lines = new ArrayList<String>();
        try {
            lines = FileUtils.readLines(new File(path+"Elite96.txt"));
        } catch (IOException ex) {
            Logger.getLogger(WikiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        int iter = 1;
        while(true) {
            int failure = 0;
            int success = 0;
            for (int i = 0; i < lines.size(); i++) {
                if (new File(path+"WikiPages/" + (i + 1) + ".txt").exists()) continue;
                try {
                    String univName = lines.get(i).split("==")[0];
                    String urlPart = univName.replaceAll(" ", "_");
                    if (urlPart.contains("&")) {
                        urlPart = urlPart.replaceAll("&", "%26");
                    }
                    String wikiURL = "http://en.wikipedia.org/wiki/" + urlPart;
                    //delay(3);
                    print("Downloading " + (i + 1) + "...");
                    Document doc = Jsoup.connect(wikiURL).userAgent("Chrome").timeout(0).get();
                    FileUtils.writeStringToFile(new File(path+"WikiPages/" + (i + 1) + ".txt"), doc.html());
                    success++;
                } catch (IOException ex) {
                    print("Skipping " + (i + 1) + " due to timeout...");
                    failure++;
                    continue;
                }
            }
            print("Iteration "+iter+": "+success+" succeeded, "+failure+" remaining...");
            if (failure == 0) break;
        }
    }
}
