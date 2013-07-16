/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Terry
 */
public class TestSchool {
    public static void main(String[] args) throws MalformedURLException, IOException {
        //Document doc = Jsoup.connect("http://www.capella.edu/").timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
        Link l = new Link();
        l.url = "http://www.stonybrook.edu/sb/academics.shtml";
        l.context = new ArrayList<String>();
        ArrayList<String> positives = Utility.getKeywords("Group/schools.txt");
        ArrayList<String> negatives = Utility.getKeywords("Group/Negatives.txt");
        ArrayList<String> degrees = Utility.getKeywords("Group/degrees.txt");
        ListEngine engine = new ListEngine(positives, negatives, degrees);
        SemanticList list = engine.getSchools(l);
        if (list != null)
        for (String school: list.list) {
            System.out.println(school);
        }
    }
}
