/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.seagatesoft.sde.tagtreebuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Yifeng
 */
public class TestUserAgent {
    public static void main(String[] args){
        try {
            Document doc = Jsoup.connect("http://cs.illinois.edu/people/faculty").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
        } catch (IOException ex) {
            Logger.getLogger(TestUserAgent.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
