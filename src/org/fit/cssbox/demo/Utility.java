/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class Utility {
    public static ArrayList<String> deptNeg;
    public static final String[] gateway = {"academics","academic units","schools","colleges","divisions",
        "departments","department list","programs","faculty","directory","people","staff","profiles"};
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                          + "|png|tiff?|mid|mp2|mp3|mp4"
                                                          + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                          + "|doc|docx|xls|xlsx|ppt|pptx"
                                                          + "|xml"
                                                          + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    
    static {
        try {
            deptNeg = getKeywords("DeptNag.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean containUrl(ArrayList<String> urls, String test){
        for (String url: urls) {
            if (url.equalsIgnoreCase(test)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean shouldVisit(String url, Set<String> visited) {
        url = url.toLowerCase();
        if (url.contains(".edu") && url.startsWith("http")&& !url.endsWith("#")&& !FILTERS.matcher(url).matches() &&
            !url.contains("admission") && !url.contains("faculty")) 
        {
            for (String str: visited) {
                if (str.equalsIgnoreCase(url)) {
                    return false;
                }
            }
            return true;
        } else return false;
    }
    
    public static boolean isDomainLink(String url) {
        if (url == null) {
            return true;
        } else {
            url = url.toLowerCase();
            if (!url.contains(".com") && !url.contains(".org") && !FILTERS.matcher(url).matches()) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    
    
    public static ArrayList<Link> getDeptLinks(Link link, Set<String> visited){
        ArrayList<Link> links = new ArrayList<Link>();
        links.add(link);
        ArrayList<Link> parents = sortLinks(getNavLinks(link,visited));
        links.addAll(parents);
        for (Link parent: parents) {
            links.addAll(sortLinks(getNavLinks(parent,visited)));
        }  
        //for (String link: links) System.out.println(link);
        return links;
    }
    
    public static ArrayList<Link> sortLinks(Set<Link> links){
        ArrayList<Link> sort = new ArrayList<Link>();
        ArrayList<Link> medPrio = new ArrayList<Link>();
        ArrayList<Link> lowPrio = new ArrayList<Link>();
        for (Link link: links){
            if (link.url.toLowerCase().contains("departments")){
                sort.add(link);
            } else if (!link.url.toLowerCase().contains("graduate") || !link.url.toLowerCase().contains("undergrad")
                    || !link.url.toLowerCase().contains("special") || !link.url.toLowerCase().contains("summer")
                    || !link.url.toLowerCase().contains("online") || !link.url.toLowerCase().contains("international")
                    || !link.url.toLowerCase().contains("affiliate") || !link.url.toLowerCase().contains("additional")) {
                medPrio.add(link);
            } else {
                lowPrio.add(link);
            }
        }
        sort.addAll(medPrio);
        sort.addAll(lowPrio);
        //for (String str: sort) {System.out.println(str);}
        return sort;
    }
    
    public static Set<Link> getNavLinks(Link link, Set<String> visited){
        Set<Link> deptLinks = new HashSet<Link>();
        try {       
            Document doc = Jsoup.connect(link.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e : result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if (anchor.split(" ").length <= 6 && (anchor.contains("departments") || anchor.contains("academics") || anchor.contains("department list")
                        || anchor.contains("programs") || anchor.contains("academic units")
                        || anchor.contains("about the college") || anchor.contains("divisions")) && shouldVisit(href, visited)) {
                    Link newLink = new Link();
                    newLink.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(link.context);
                    context.add(anchor);
                    newLink.context = context;
                    deptLinks.add(newLink);
                }
            }
            visited.addAll(Utility.getVisited(link.url));
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return new HashSet<Link>();
        }
        return deptLinks;
    }
    
    
    
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
     
    
    public static boolean contains(ArrayList<String> keywords, String text, String[] temps) {
        String[] tokens = text.split(" ");
        for (String keyword: keywords) {
            if (keyword.length() > 5 && text.toUpperCase().contains(keyword.toUpperCase())){
                temps[0] = keyword.toUpperCase();
                return true;
            } else if (keyword.length() <= 5) {
                for (String token : tokens) {
                    if (token.replaceAll(",", "").equalsIgnoreCase(keyword)) {
                        temps[0] = keyword.toUpperCase();
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean negative(String text,String url) {
        boolean result = false;
        if (!text.matches("\\D*") || text.toUpperCase().contains("PROGRAM") || text.toUpperCase().contains("DEPARTMENT") || text.toUpperCase().contains("OFFICE") || text.toUpperCase().contains("CENTER") || text.toUpperCase().contains("MAJOR") || text.toUpperCase().contains("MINOR") || text.toUpperCase().contains("CATALOG") || text.toUpperCase().contains("FACULTY") || text.toUpperCase().contains("ONLINE")
               || text.toUpperCase().contains("BACHELOR") || text.toUpperCase().contains("ASSOCIATE") || text.toUpperCase().contains("MASTER") || text.toUpperCase().contains("MBA")|| text.toUpperCase().contains("STUDENT")|| text.toUpperCase().contains("TEST")) {
            result = true;
        }
        if (url !=null && (!url.toUpperCase().contains(".EDU") || url.toUpperCase().contains("PROGRAM") || 
                url.toUpperCase().contains("APPLY") || url.toUpperCase().contains("RESEARCH") || 
                url.toUpperCase().contains("ADMISSION") || url.toUpperCase().contains("LIBRARY") ||
                url.toUpperCase().contains("TOPIC"))) {
            result = true;
        }
        return result;
    }
    
    public static boolean validCombo(ArrayList<Combo> combos, ArrayList<String> keywords) {
        int count = 0;
        int negCount = 0;
        int prefixCount = 0;
        Set<String> keys = new TreeSet<String>();
        for (Combo c: combos) {
            String[] temps = new String[1];
            if (c.text.split(" ").length <9 && contains(keywords,c.text,temps) && !negative(c.text,c.url)) {
                count++;
                keys.add(temps[0]);
            }
            if (c.text.contains("Departments") || c.text.contains("Undergraduate") || c.text.contains("Graduate") || c.text.contains("Professional") || c.text.contains("Education") || c.text.contains("Master")
                    || c.text.contains("About") || c.text.contains("Pre-")) {
                negCount++;
            }
            if (c.text.toLowerCase().contains("school of") || c.text.toLowerCase().contains("college of")
                    || c.text.toLowerCase().endsWith("school")) {
                prefixCount++;
            }
        }
        //for (Combo c : combos) {
            //System.out.println(c);
        //}
        //System.out.println("---------------------------------------");
        //System.out.println(keys.size()+" "+negCount+" "+count+" "+ combos.size());
        if (prefixCount > combos.size()*2/3) {
            return true;
        }
        if (keys.size() <=2) return false;
        if (negCount > combos.size()/2) return false;
        if (count >= (float)combos.size()*0.67) {
            return true;
        } else return false;
    }
    public static boolean negative2(String text,String url) {
        boolean result = false;
        if (!text.matches("\\D*") ) {
            result = true;
        }
        for (String neg: deptNeg) {
            if (text.contains(neg)) {
                result = true;
                break;
            }
        }
        if (url !=null && (url.toUpperCase().contains("NEWS") ||url.toUpperCase().contains("APPLY") || 
                url.toUpperCase().contains("RESEARCH") || url.toUpperCase().contains("ADMISSION") ||
                url.toUpperCase().contains("LIBRARY") || url.toUpperCase().contains("MAJOR") ||
                url.toUpperCase().contains("BACHELOR") || url.toUpperCase().contains("CATALOG") ||
                url.toUpperCase().contains("ABOUT") || url.toUpperCase().contains("VISIT") ||
                url.toUpperCase().contains("MINOR")|| url.toUpperCase().contains("PROGRAM")|| url.toUpperCase().contains("EVENT")
                || url.toUpperCase().contains("DEGREE")|| url.toUpperCase().contains("MASTER")|| url.toUpperCase().contains("SPECIALIZATION")
                || url.toUpperCase().contains("GRADUATE")|| url.toUpperCase().contains("CERTIFICATE")
                || url.contains("BS")|| url.contains("PhD")|| url.contains("MS")|| url.contains("MBA")
                || url.contains("MPA"))) {
            result = true;
        }
        return result;
    }
    
    public static boolean isValidResult(ArrayList<String> dedups){
        //if (true) return true;
        if (dedups.size()<3) return false;
        int count = 0;
        for (String dedup: dedups){
            String[] pair = dedup.split("==");
            if (pair.length==2){
                String url = pair[1]; //|| url.toUpperCase().contains("PROGRAM")
                if (url != null && (url.toUpperCase().contains("NEWS") || url.toUpperCase().contains("APPLY")
                        || url.toUpperCase().contains("RESEARCH") || url.toUpperCase().contains("ADMISSION")
                        || url.toUpperCase().contains("LIBRARY") || url.toUpperCase().contains("MAJOR")
                        || url.toUpperCase().contains("BACHELOR") || url.toUpperCase().contains("CATALOG")
                        || url.toUpperCase().contains("MINOR") || url.toUpperCase().contains("EVENT")
                        || url.toUpperCase().contains("CONTACT") || url.toUpperCase().contains("SUMMER")
                        || url.toUpperCase().contains("DEGREE") || url.toUpperCase().contains("MASTER") || url.toUpperCase().contains("SPECIALIZATION")
                        || url.toUpperCase().contains("GRADUATE") || url.toUpperCase().contains("CERTIFICATE")
                        || url.contains("BS") || url.contains("PhD") || url.contains("MS") || url.contains("MBA")
                        || url.contains("MPA")|| url.toLowerCase().contains("film"))) {
                    count++;
                }
            }
        }
        if (count > dedups.size()/2) {
            return false;
        }
        return true;
    }
    
    public static boolean validCombo2(ArrayList<Combo> combos, ArrayList<String> keywords) {
        //System.out.println(combos.get(0).previous);
       /* if (combos.get(0).previous != null && ((//combos.get(0).previous.text.toLowerCase().contains("programs") ||
                combos.get(0).previous.text.toLowerCase().contains("majors") ||
                combos.get(0).previous.text.toLowerCase().contains("minors") ||
                combos.get(0).previous.text.toLowerCase().contains("degrees") ||
                combos.get(0).previous.text.toLowerCase().contains("outreach")))
                 && !combos.get(0).previous.text.toLowerCase().contains("departments")) return false; */
        //for (Combo c: combos) System.out.println(c);
        //System.out.println("---------------------");
        //if (combos.get(0).previous != null && combos.get(0).previous.text.toLowerCase().contains("ongoing")) {
            //return false;
        //}
        int count = 0;
        Set<String> keys = new TreeSet<String>();
        for (Combo c: combos) {
            String[] temps = new String[1];
            if (c.text.split(" ").length <9 && contains(keywords,c.text, temps) && !negative2(c.text, c.url)) {
                count++; 
                //System.out.println(c.text);
                keys.add(temps[0]);
            }
        }
        //if (combos.get(0).x == 41)System.out.println(count+"/"+combos.size());
        if (count >= (float)combos.size()*0.67) {
            return true;
        } else return false;
    }
    
    public static boolean specialCombo2(ArrayList<Combo> combos, ArrayList<String> keywords) {
        //System.out.println(combos.get(0).previous);
        //System.out.println(combos.size());
        if (combos.get(0).previous != null && ((combos.get(0).previous.text.toLowerCase().contains("programs") ||
                combos.get(0).previous.text.toLowerCase().contains("majors") ||
                combos.get(0).previous.text.toLowerCase().contains("minors") ||
                combos.get(0).previous.text.toLowerCase().contains("degrees")))
                 && !combos.get(0).previous.text.toLowerCase().contains("departments")) return false;
        int count = 0;
        Set<String> keys = new TreeSet<String>();
        int deptCount = 0;
        for (Combo c: combos) {
            //System.out.println(c);
            String[] temps = new String[1];
            char init = 'a';
            if (c.text.length()>0) {
                init = c.text.charAt(0);
            }
            if (init>='A' && init<='Z'&&c.text.split(" ").length <9 && contains(keywords,c.text, temps) && !negative2(c.text, c.url)) {
                count++; 
                keys.add(temps[0]);
            }
            if (init>='A' && init<='Z'&&c.text.split(" ").length <9 && (c.text.contains("Dept.") || c.text.contains("Department") ||
                   c.text.contains("School") || c.text.contains("Division"))&& contains(keywords,c.text, temps)){
                deptCount++;
            }
        }
        //System.out.println("---------------------------------------");
        
        if (keys.size() <=2 && combos.size() > 3) return false;
        
        //System.out.println(count+"/"+combos.size());
        if (count > combos.size()/2) {
            //System.out.println("why..");
            return true;
        } else if (deptCount > 4){
            return true;
        }else return false;
    }
    
    public static boolean similar(int first, int second) {
        boolean result = false;
        if (first <= second*2 && first >= second/2) {
            result = true;
        }
        return result;
    }
    
    public static void group(ArrayList<Combo> combos) {
        if (combos.size() >= 3) {
            int[] intervals = new int[combos.size() - 1];
            for (int i = 0; i < combos.size() - 1; i++) {
                intervals[i] = Math.abs(combos.get(i + 1).y - combos.get(i).y) - combos.get(i).contract;
                //if (combos.get(0).x == 116) System.out.println(combos.get(i).contract);
            } 
            //System.out.println(Arrays.toString(intervals));
            for (int k=0;k<intervals.length-1;k++) {
                if (similar(intervals[k], intervals[k+1])) {
                    combos.get(k).setGroup(1);
                    combos.get(k+1).setGroup(1);
                    combos.get(k+2).setGroup(1);
                    int group = 1;
                    for (int j = k+3; j < combos.size(); j++) {
                        if (intervals[j - 1] > intervals[j - 2] * 2) {
                            group++;
                            combos.get(j).setGroup(group);
                        } else {
                            combos.get(j).setGroup(group);
                        }
                    }
                    break;
                }
            }  
        } else {
            for (Combo c: combos) {
                c.setGroup(1);
            }
        }
    }
    
    public static void groupExact(ArrayList<Combo> combos) {
        //bubble(combos);
        if (combos.size() >= 3) {
            int[] intervals = new int[combos.size() - 1];
            for (int i = 0; i < combos.size() - 1; i++) {
                intervals[i] = Math.abs(combos.get(i + 1).y - combos.get(i).y) - combos.get(i).contract;
            } 
            for (int k=0;k<intervals.length-1;k++) {
                if (intervals[k] == intervals[k+1]) {
                    combos.get(k).setGroup(1);
                    combos.get(k+1).setGroup(1);
                    combos.get(k+2).setGroup(1);
                    int group = 1;
                    for (int j = k+3; j < combos.size(); j++) {
                        if (intervals[j - 1] > intervals[j - 2]) { //intervals[j - 1] != intervals[j - 2]
                            group++;
                            combos.get(j).setGroup(group);
                        } else {
                            combos.get(j).setGroup(group);
                        }
                    }
                    break;
                }
            }  
        }else {
            for (Combo c: combos) {
                c.setGroup(1);
            }
        }
    }
    
    public static boolean contains(ArrayList<String> keywords, String text) {
        String[] tokens = text.split(" ");
        for (String keyword: keywords) {
            if (keyword.length() > 5 && text.toUpperCase().contains(keyword.toUpperCase())){
                return true;
            } else if (keyword.length() <= 5) {
                for (String token : tokens) {
                    if (token.replaceAll(",", "").equalsIgnoreCase(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public static boolean isValid(Elements items, ArrayList<String> keywords){
        int count = 0;
        for (Element item: items) {
            String text = item.text();
            String url = item.absUrl("href");
            //System.out.println(text);
            if (text.split(" ").length <9 && contains(keywords,text) && !negative2(text, url)) {
                count++; 
            }
        }
        //System.out.println("-------------------------");
        //if (combos.get(0).x == 41)System.out.println(count+"/"+combos.size());
        if (count >= (float)items.size()*0.66) {
            return true;
        } else return false;
    }
    
    
    public static HashMap<String,ArrayList<Combo>> vertical(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + ""+ c.style;
                //if (c.text.equals("School of Business")) System.out.println(c);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> verticalNames(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + "" + c.style;
                //System.out.println(c.style);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> horizontal(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.y + "" + c.height + ""+ c.style;
                //System.out.println(c);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> arithmetic(ArrayList<Combo> combos, int grouptype) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 3) {
                if (grouptype == 1) {
                    group(temps);
                } else groupExact(temps);
                //System.out.println("Vertical:");
                for (Combo t : temps) {
                    //System.out.println(t);
                }
                for (Combo c : temps) {
                    if (c.group != 0) {
                        String newKey = key + "" + c.group;
                        if (results.keySet().contains(newKey)) {
                            results.get(newKey).add(c);
                        } else {
                            ArrayList<Combo> empty = new ArrayList<Combo>();
                            empty.add(c);
                            results.put(newKey, empty);
                        }
                    }
                }
            }
        }
        return results;
    }

    public static HashMap<String,ArrayList<Combo>> tiled(ArrayList<Combo> combos, int grouptype) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height+ "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 1) {
                if (grouptype == 1) {
                    group(temps);
                } else groupExact(temps);
                for (Combo c : temps) {
                    //System.out.println(c);
                    if (c.group != 0) {
                        //if (c.text.equals("School of Business")) System.out.println(c);
                        String newKey = key + "" + c.group;
                        if (results.keySet().contains(newKey)) {
                            results.get(newKey).add(c);
                        } else {
                            ArrayList<Combo> empty = new ArrayList<Combo>();
                            empty.add(c);
                            results.put(newKey, empty);
                        }
                    }
                }
            }
        }
        return results;
    }
    
    public static HashMap<String,ArrayList<Combo>> tiled(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height+ "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 1) {
                float total = 0;
                for (int i=0;i<temps.size()-1;i++) {
                    total += temps.get(i+1).y - temps.get(i).y - temps.get(i).contract;
                }
                float gap = total/(temps.size()-1);
                if (gap > 50) {
                    results.put(key, temps);
                }
            }
        }
        return results;
    }
    
    public static Set<String> getVisited(String url){
        Set<String> deptLinks = new HashSet<String>();
        try { 
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            }
            Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e : result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if (anchor.split(" ").length <= 6 && isGateway(anchor)) {
                    deptLinks.add(href);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return new HashSet<String>();
        }
        return deptLinks;
    }
    
    private static boolean isGateway(String anchor) {
        for (String gate: gateway) {
            if (anchor.contains(gate)) {
                return true;
            }
        }
        return false;
    }
}
