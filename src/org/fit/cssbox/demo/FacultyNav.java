/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class FacultyNav{

    
    public static String getExactLink(ArrayList<String> links, String keyword) {
        for (String link: links) {
            String[] tokens = link.split("==");
            if (tokens[0].equalsIgnoreCase(keyword)) {
                return tokens[1];
            }
        }
        return null;
    }
    
    
    
    public static ArrayList<String> getLinks(String url, Set<String> visited) {
        try {
            ArrayList<String> links = new ArrayList<String>();
            Document doc = Jsoup.connect(url).timeout(12000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e : result) {
                String anchor = e.text().trim();
                String href = e.attr("abs:href").trim();
                if (!href.equals("") && !anchor.equals("") && Utility.shouldVisit(href, visited)) {
                    links.add(anchor+"=="+href);
                }
            }
            return links;
        } catch (IOException ex) {
            Logger.getLogger(FacultyNav.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<String>();
        }
    }

    
    public static ArrayList<String> getFacultyLinks(ArrayList<String> links) {
        ArrayList<String> facLinks = new ArrayList<String>();
        int count = 0;
        for (String link: links) {
            String anchor = link.split("==")[0];
            if (count <2 && anchor.split(" ").length >= 2 && anchor.split(" ").length <=4 && anchor.toLowerCase().contains("faculty") 
                    && !anchor.toLowerCase().contains("adjunct")&& !anchor.toLowerCase().contains("emeriti") && !anchor.toLowerCase().contains("awards")
                    && !anchor.toLowerCase().contains("publication")) { 
                facLinks.add(link.split("==")[1]);
                count++;
            }
        }
        return facLinks;
    }
    
    public static String getFacultyAndStaff(ArrayList<String> links) {
        for (String link: links) {
            String[] tokens = link.split("==");
            if (tokens[0].toLowerCase().contains("faculty") && tokens[0].toLowerCase().contains("staff")) {
                return tokens[1];
            }
        }
        return null;
    }
    
    /*
    public static ArrayList<String> getFacultyListURL(String url, Set<String> visited) throws IOException {
        ArrayList<String> candidates = new ArrayList<String>();;
        ArrayList<String> links = getLinks(url, visited);
        String facultyURL = getExactLink(links, "Faculty");
        String peopleURL = getExactLink(links, "People");
        String directoryURL = getExactLink(links, "Directory");
        String facultyStaffURL = getFacultyAndStaff(links);
        if (facultyURL != null) {
            candidates.add(facultyURL);
            candidates.addAll(getFacultyLinks(getLinks(facultyURL,visited)));
        }
        if (peopleURL != null) {
            candidates.add(peopleURL);
            String facultyURL2 = getExactLink(getLinks(peopleURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
            }
        }
        if (facultyStaffURL != null) {
            candidates.add(facultyStaffURL);
            String facultyURL2 = getExactLink(getLinks(facultyStaffURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
            }
        }
        if (directoryURL != null) {
            candidates.add(directoryURL);
            String facultyURL2 = getExactLink(getLinks(directoryURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
            }
        }
        ArrayList<String> temps = getFacultyLinks(links);
        candidates.addAll(temps);
        for (String temp: temps) {
            candidates.addAll(getFacultyLinks(getLinks(temp,visited)));
        }
        return candidates;
    } */
    
    public static ArrayList<String> getFacultyListURL(String url, Set<String> visited) throws IOException {
        ArrayList<String> candidates = new ArrayList<String>();;
        ArrayList<String> links = getLinks(url, visited);
        String facultyURL = getExactLink(links, "Faculty");
        String peopleURL = getExactLink(links, "People");
        String directoryURL = getExactLink(links, "Directory");
        String facultyStaffURL = getFacultyAndStaff(links);
        if (facultyURL != null) {
            candidates.add(facultyURL);
            visited.add(facultyURL);
            ArrayList<String> seconds = getFacultyLinks(getLinks(facultyURL,visited));
            candidates.addAll(seconds);
            visited.addAll(seconds);
        }
        if (peopleURL != null) {
            candidates.add(peopleURL);
            visited.add(peopleURL);
            String facultyURL2 = getExactLink(getLinks(peopleURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(facultyURL2);
            }
        }
        if (facultyStaffURL != null) {
            candidates.add(facultyStaffURL);
            visited.add(facultyStaffURL);
            String facultyURL2 = getExactLink(getLinks(facultyStaffURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(facultyURL2);
            }
        }
        if (directoryURL != null) {
            candidates.add(directoryURL);
            visited.add(directoryURL);
            String facultyURL2 = getExactLink(getLinks(directoryURL,visited), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(directoryURL);
            }
        }
        ArrayList<String> temps = getFacultyLinks(links);
        candidates.addAll(temps);
        visited.addAll(temps);
        for (String temp: temps) {
            ArrayList<String> seconds = getFacultyLinks(getLinks(temp,visited));
            candidates.addAll(seconds);
            visited.addAll(seconds);
        }
        return candidates;
    }
    
    public static String getFacultyURL(String url, Set<String> visited) throws IOException {
        if (url == null) return null;
        ArrayList<String> candidates = getFacultyListURL(url, visited);
        for (String candidate: candidates) {
            System.out.println(candidate);
            if (candidate != null && Faculty2.identify(candidate)) {
                return candidate;
            }
        }
        return null;
    }
    
}
