/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class LinkFinder {
    public static void main(String[] args) throws IOException {
        Document doc = Jsoup.connect("http://www.bgsu.edu/colleges/technology/departments_programs.html").get();
        Elements target = doc.getElementsMatchingOwnText("^Department of Construction Management$");
        if (target.size()>0) {
            Element anchor = target.get(0);
            Element url = anchor.nextElementSibling();
            System.out.println(anchor.absUrl("href"));
            Elements hrefs = url.select("a");
            if (hrefs.size()>0) {
                System.out.println(hrefs.get(0).absUrl("href"));
            } else {
                Element url2 = url.nextElementSibling();
                System.out.println(url2.text());
                Elements hrefs2 = url2.select("a");
                if (hrefs2.size() > 0) {
                    System.out.println(hrefs2.get(0).absUrl("href"));
                }
            }
        }
    }
}
