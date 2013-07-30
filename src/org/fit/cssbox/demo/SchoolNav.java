/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
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
public class SchoolNav{
    private static ListEngine engine;
    
    static {
        try {
            ArrayList<String> positives = Utility.getKeywords("Group/schools.txt");
            ArrayList<String> negatives = Utility.getKeywords("Group/Negatives.txt");
            ArrayList<String> degrees = Utility.getKeywords("Group/degrees.txt");
            engine = new ListEngine(positives, negatives, degrees);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Link getLink(Link url, String keyword) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        Link link = null;
        try {
            Document doc = Jsoup.connect(url.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) {
                String anchor = e.text().trim();
                String href = e.attr("abs:href").trim();
                if (anchor.contains(keyword) && !href.equals("") && Utility.shouldVisit(href, new HashSet<String>())) {
                    link = new Link();
                    link.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(url.context);
                    context.add(anchor);
                    link.context = context;
                    break;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return link;
    }
    
    public static boolean needURL(ArrayList<String> temps) {
        for (String temp: temps) {
            if (temp.split("==").length==2 && temp.split("==")[1].equals("null")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean needURLStrong(ArrayList<String> temps) {
        for (String temp: temps) {
            if (temp.split("==").length==2 && !temp.split("==")[1].equals("null")) {
                return false;
            }
        }
        return true;
    }
    
        
    public static ArrayList<String> getLinks(Document doc) throws IOException {
        ArrayList<String> links = new ArrayList<String>();
        Elements result = doc.select("a");
        for (Element e : result) {
            String anchor = e.text().trim();
            String href = e.attr("abs:href").trim();
            if (!href.equals("")) {
                links.add(anchor+"=="+href);
            }
        }
        return links;
    } 
    
    public static void addURLs(ArrayList<String> temps, String baseURL) throws IOException{
        Document doc = Jsoup.connect(baseURL).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
        //////////////////////////////the same anchor-global/////////////////////////////////////
        ArrayList<String> links = null;
        
        if (needURL(temps)) {  
            //for (String temp: temps) System.out.println(temp);
            links = getLinks(doc);
            for (int i=0; i< temps.size(); i++){
                //System.out.println(temps.get(i));
                String[] tokens = temps.get(i).split("==");
                if (tokens[1].equals("null")) {
                    //System.out.println(tokens[0]);
                    for (String link: links) {
                        String[] pair = link.split("==");
                        if (pair[0].equalsIgnoreCase(tokens[0]) && !pair[1].contains("#")){
                            temps.set(i,tokens[0]+"=="+pair[1]);
                            break;
                        }
                    }
                }
            }
        } else return; 
        
        /////////////////////////////similar global////////////////////
        if (needURL(temps)) {
            for (int i=0; i< temps.size(); i++){
                //System.out.println(temps.get(i));
                String[] tokens = temps.get(i).split("==");
                if (tokens[1].equals("null")) {
                    //System.out.println(tokens[0]);
                    for (String link: links) {
                        
                        String[] pair = link.split("==");
                        String anchor = pair[0].replaceAll(".*\\s+of\\s+", "").replaceAll("&", "and").replaceAll(":", "").trim();
                        String abbrevation = tokens[0].replaceAll(".*\\s+of\\s+", "").replaceAll("&", "and").replaceAll(":", "").trim();
                        if (anchor.endsWith("of")) {
                            anchor = anchor.split(",")[0];
                        }
                        //System.out.println(anchor);
                        if (anchor.equalsIgnoreCase(abbrevation) && !pair[1].contains("#")){
                            temps.set(i,tokens[0]+"=="+pair[1]);
                            break;
                        }
                        /*
                        if (GetDepartmentURL.similar(anchor,abbrevation) && !pair[1].contains("#")){
                            temps.set(i,tokens[0]+"=="+pair[1]);
                            break;
                        }*/
                    }
                }
            }
        } else return;
        
        //for (String temp: temps) System.out.println(temp);
        ///////////////////////////parallel local/////////////////////////
        if (needURLStrong(temps)) {          
            //System.out.println(temps.size());
            ArrayList<String> as = GetDepartmentURL.parallelLinks(baseURL, temps.size());
            if (as.size() != 0){
                //System.out.println("strong....");
                for (int i=0; i< temps.size(); i++){
                    String[] tokens = temps.get(i).split("==");
                    temps.set(i,tokens[0]+"=="+as.get(i));
                }
            }
        } //remove return part
        
        //////////////////////////local////////////////////////////////////
        //for (String temp: temps) System.out.println(temp);
        GetDepartmentURL departmentUrl = new GetDepartmentURL();
        if (needURL(temps)) {
            for (int i=0; i< temps.size(); i++){
                String[] tokens = temps.get(i).split("==");
                if (tokens[1].equals("null")) {
                    String match = departmentUrl.getDepartmentURL(doc,tokens[0]);
                    if (match != null) {
                        temps.set(i,tokens[0]+"=="+match);
                        //System.out.println(temps.get(i));
                    }
                }
            }
        } else return;
    }
    
    public static ArrayList<String> dedup(ArrayList<String> dups, String baseURL) {
        /*
        try {
            addURLs(dups,baseURL);
        } catch (IOException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        ArrayList<String> urls = new ArrayList<String>();
        ArrayList<String> dedups = new ArrayList<String>();
        for (String dup: dups) {
            if (dup.split("==").length!=2) continue;
            //System.out.println(dup);
            String url = dup.split("==")[1];
            if (url.equals("null")) {
                dedups.add(dup);
            } else 
            if (!urls.contains(url)) { 
                urls.add(url);
                dedups.add(dup);
            }
        }
        return dedups;
    }
     
    
    public static ArrayList<Link> getSchoolLinks(Link url) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<Link> schoolLinks = new ArrayList<Link>();
        try {
            Document doc = Jsoup.connect(url.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if ((anchor.contains("colleges") || anchor.contains("schools") || anchor.contains("faculties")) && !href.equals("") && Utility.shouldVisit(href, new HashSet<String>())) {
                    Link link = new Link();
                    link.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(url.context);
                    context.add(anchor);
                    link.context = context;
                    schoolLinks.add(link);
                } else {
                    Elements images = e.select("img");
                    if (images.size()>0) {
                        for (Element image: images) {
                            String alt = image.attr("alt").toLowerCase();
                            String title = image.attr("title").toLowerCase();
                            if ((alt.contains("schools")||alt.contains("colleges")||alt.contains("faculties")||title.contains("schools")||title.contains("colleges")||title.contains("faculties")) &&
                                 !href.equals("") && Utility.shouldVisit(href, new HashSet<String>())) {
                                Link link = new Link();
                                link.url = href;
                                ArrayList<String> context = new ArrayList<String>();
                                context.addAll(url.context);
                                if (alt.contains("schools")||alt.contains("colleges")||alt.contains("faculties")) {
                                    context.add(alt);
                                } else {
                                    context.add(title);
                                }
                                link.context = context;
                                schoolLinks.add(link);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return schoolLinks;
    }
    
    public static ArrayList<Link> getAcademicLinks(Link url) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<Link> schoolLinks = new ArrayList<Link>();
        try {
            Document doc = Jsoup.connect(url.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if ((anchor.contains("academics") || anchor.contains("academic units") || anchor.contains("academic divisions") || anchor.contains("academic areas")) && !href.equals("") && Utility.shouldVisit(href, new HashSet<String>())) {
                    Link link = new Link();
                    link.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(url.context);
                    context.add(anchor);
                    link.context = context;
                    schoolLinks.add(link);
                } else {
                    Elements images = e.select("img");
                    if (images.size()>0) {
                        for (Element image: images) {
                            String alt = image.attr("alt").toLowerCase();
                            String title = image.attr("title").toLowerCase();
                            if ((alt.contains("academics")||alt.contains("academic units")||alt.contains("academic divisions")||alt.contains("academic areas")
                                    ||title.contains("academics")||title.contains("academic units")||title.contains("academic divisions")||title.contains("academic areas")) &&
                                 !href.equals("") && Utility.shouldVisit(href, new HashSet<String>())) {
                                Link link = new Link();
                                link.url = href;
                                ArrayList<String> context = new ArrayList<String>();
                                context.addAll(url.context);
                                if (alt.contains("academics")||alt.contains("academic units")||alt.contains("academic divisions")||alt.contains("academic areas")) {
                                    context.add(alt);
                                } else {
                                    context.add(title);
                                }
                                link.context = context;
                                schoolLinks.add(link);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
        return schoolLinks;
    }
    
    public static ArrayList<Link> dedupNavLinks(ArrayList<Link> dups) {
        ArrayList<Link> dedups = new ArrayList<Link>();
        ArrayList<String> urls = new ArrayList<String>();
        for (Link dup: dups) {
            if (!urls.contains(dup.url)) {
                dedups.add(dup);
                urls.add(dup.url);
            }
        }
        return dedups;
    }
    
    public static ArrayList<Link> getNavLinks(Link url) {
        ArrayList<Link> navLinks = new ArrayList<Link>();
        navLinks.add(url); //homepage
        ArrayList<Link> schoolLinks = getSchoolLinks(url);
        navLinks.addAll(schoolLinks);
        ArrayList<Link> academicLinks = getAcademicLinks(url);
        navLinks.addAll(academicLinks);
        for (Link link: academicLinks) {
            navLinks.addAll(getSchoolLinks(link));
        }
        return dedupNavLinks(navLinks);
    }
    
    public static SemanticList getSchoolsResult(Link link, Set<String> visited) throws IOException {
        ArrayList<SemanticList> lists = new ArrayList<SemanticList>();
        SemanticList schools = null;
        ArrayList<Link> toSchedule = getNavLinks(link); 
        //System.out.println(toSchedule.size());
        for (int i=0;i<toSchedule.size();i++){
            //System.out.println(l.url);
            schools = engine.getSchools(toSchedule.get(i));
            visited.addAll(Utility.getVisited(toSchedule.get(i).url));
            if (schools != null) {
                //System.out.println("oh yeah");
                lists.add(schools);
            }
        }      
        
        if (lists.size() > 0) {
            //System.out.println("oh yeah");
            for (SemanticList list : lists) {
                ArrayList<String> items = list.list;
                for (String item: items) {
                    if (item.toLowerCase().contains("school") || item.toLowerCase().contains("college") || item.toLowerCase().contains("faculty")){
                        return list;
                    }
                }
            }
            for (SemanticList list : lists) {
                String head = list.head;
                String title = list.title;
                if (head == null) head = "";
                if (title == null) title = "";
                if (head.toLowerCase().contains("schools") || head.toLowerCase().contains("colleges")
                        || head.toLowerCase().contains("divisions")|| head.toLowerCase().contains("faculties") || title.toLowerCase().contains("schools")
                        || title.toLowerCase().contains("colleges") || title.toLowerCase().contains("divisions") || title.toLowerCase().contains("faculties")) {
                    return list;
                }
            }
            for (SemanticList list : lists) {
                ArrayList<String> context = list.context.context;
                if (context.size()>0) {
                    String anchor = context.get(0).toLowerCase();
                    if (anchor.contains("schools") || anchor.contains("colleges") || anchor.contains("faculties")) {
                        return list;
                    }
                }
            }
            return lists.get(0);
        } else {
            return null;
        }

    }
}
