/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class Engine2 extends Thread {
    String filename;

    public Engine2(String filename) {
        this.filename = filename;
    }
    
    public static String getLink(String url, String keyword) {
        String link = null;
        try {
            Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) 
            {
                String anchor = e.text().trim();
                String href = e.attr("abs:href").trim();
                if (anchor.toUpperCase().contains(keyword.toUpperCase()) && anchor.split(" ").length<=3 && !href.equals("") && !href.contains("@"))
                {
                    link = href;
                    if(!link.equals(url))
                    break;
                }
            }
        } catch (IOException ex) {
            
        }
        return link;
    }
    
    public void run() {
        try {
            StringBuilder sb = new StringBuilder();
            ArrayList<String> schools = Utility.getKeywords("Univs2//"+filename);
            for (int i=1;i<schools.size();i++) {
                sb.append("School=="+schools.get(i)+"\n");
                String url = schools.get(i).split("==")[1];
                if (!url.equals("null")) {
                    ArrayList<String> depts = Sort2.getDepts(url);
                    if (depts.size()<4) {
                        String link = getLink(url,"Departments");
                        if (link != null) {
                            depts = Sort2.getDepts(link);
                            for (String dept : depts) {
                                sb.append("Dept==" + dept + "\n");
                            }
                        }
                    } else {
                        for (String dept : depts) {
                            sb.append("Dept==" + dept + "\n");
                        }
                    }
                }
                sb.append("\n");
            }
            FileUtils.writeStringToFile(new File("Schools2//"+filename), sb.toString());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Engine2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Engine2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        File[] files = new File("Univs2").listFiles();
        Engine2[] threads = new Engine2[files.length];
        for (int i=0;i<files.length;i++) {
            threads[i]=new Engine2(files[i].getName());
            threads[i].start();
        }
        for (Engine2 thread: threads) {
            thread.join();
        }
    }
}
