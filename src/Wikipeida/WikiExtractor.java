/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wikipeida;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    
    public static void extractTable(String file, String univName, StringBuilder sb) throws IOException{
        String html = FileUtils.readFileToString(new File(file),"utf-8");
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table");
        String motto = "";
        String established = "";
        String type = "";
        String endowment = "";
        String president = "";
        String location = "";
        String campus = "";
        String logo = "";
        Element attrTable = attrTable(tables);
        if (attrTable != null) {
            if (attrTable.select("img").size()>0) {
                logo = attrTable.select("img").attr("src");
            }
            Elements trs = attrTable.select("tr");
            for (Element tr : trs) {
                String th = tr.select("th").text();
                String td = tr.select("td").text();
                if (th.equals("Motto in English")) {
                    motto = td.replaceAll("\\[\\d\\]", "");
                } else if (th.equals("Established")) {
                    established = td.replaceAll("\\[.*?\\]", "");
                } else if (th.equals("Type")) {
                    type = td.replaceAll("\\[\\d\\]", "").replaceAll(" \\?", "");
                } else if (th.equals("Endowment")) {
                    endowment = td.replaceAll("\\[\\d\\]", "");
                } else if (th.equals("President") || th.equals("Principal")) {
                    president = td.replaceAll("\\[\\d\\]", "");
                }  else if (th.equals("Location")) {
                    location = td.replaceAll("\\[\\d\\]", "").replaceAll("(Canada|United States|US|U\\.S\\.).+", "$1").replaceAll("Coordinates.+", "");
                }  else if (th.equals("Campus")) {
                    campus = td.replaceAll("\\[\\d\\]", "");
                }        
            }
            if (motto.equals("")) {
                for (Element tr : trs) {
                    String th = tr.select("th").text();
                    String td = tr.select("td").text();
                    if (th.equals("Motto")) {
                        motto = td.replaceAll("\\[\\d\\]", "");
                    }
                }
            }
        }
        sb.append("Motto=="+motto+"\n");
        sb.append("Established=="+established+"\n");
        sb.append("Type=="+type+"\n");
        sb.append("Endowment=="+endowment+"\n");
        sb.append("President=="+president+"\n");
        sb.append("Location=="+location+"\n");
        sb.append("Campus=="+campus+"\n");
        //print(univName+": "+campus);
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
                if (th.text().contains("Motto") || th.text().contains("Established")) {
                    return table;
                }
            }
        }
        return null;
    }
    
    public static void download(){
        List<String> lines = new ArrayList<String>();
        try {
            lines = FileUtils.readLines(new File(path+"Elite96.txt"));
        } catch (IOException ex) {
            Logger.getLogger(WikiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        int iter = 0;
        while(true) {
            int failure = 0;
            int success = 0;
            iter++;
            for (int i = 0; i < lines.size(); i++) {
                if (new File(path+"WikiPages/" + (i + 1) + ".txt").exists()) continue;
                try {
                    String univName = lines.get(i).split("==")[0];
                    String urlPart = univName.replaceAll(" ", "_");
                    if (urlPart.contains("&")) {
                        urlPart = urlPart.replaceAll("&", "%26");
                    }
                    String wikiURL = "http://en.wikipedia.org/wiki/" + urlPart;
                    delay(3);
                    print("Downloading " + (i + 1) + "...");
                    Document doc = Jsoup.connect(wikiURL).userAgent("crawler4j (http://code.google.com/p/crawler4j/)").timeout(3000).get();
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
    
    public static void extract() throws IOException {
        List<String> lines = new ArrayList<String>();
        try {
            lines = FileUtils.readLines(new File(path+"Elite96.txt"));
        } catch (IOException ex) {
            Logger.getLogger(WikiExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        for (int i=0; i<lines.size(); i++) {
            StringBuilder sb = new StringBuilder();
            String univName = lines.get(i).split("==")[0];
            String univURL = lines.get(i).split("==")[1];
            sb.append("Name=="+univName+"\n");
            sb.append("Website=="+univURL+"\n");
            extractTable(path+"WikiPages/" + (i + 1) + ".txt", univName, sb);
            FileUtils.writeStringToFile(new File(path+"WikiResults/" + (i + 1) + ".txt"), sb.toString());
        }
    }
    
    public static void main(String[] args) throws IOException {
        String url = "http://en.wikipedia.org/wiki/Stony_Brook_University";
        Document doc = Jsoup.connect(url).get();//Jsoup.parse(new URL(url).openStream(), "utf-8", url);
        //doc.outputSettings().charset("utf-8");
        
        FileUtils.writeStringToFile(new File("Stony.txt"), doc.html(), "utf-8");
    }
}
