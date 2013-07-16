/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.fit.cssbox.demo.Utility.isValid;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



/**
 *
 * @author Terry
 */
public class Sort {
    public static ArrayList<String> keywords;
    static {
        try {
            keywords = Utility.getKeywords("schools.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ArrayList<String> extract2(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.vertical(combos);
        int height = 0;
        int xvalue = 0;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            for (Combo c: current) {
                //if ( c.x == 314 && c.tag.equals("a"))
                //System.out.println(c);
            }
            //System.out.println("---------------------------------------");
            if (current.size() < 4) continue;
            int gap = current.get(1).y - current.get(0).y;
            int gap2 = current.get(2).y - current.get(1).y;
            int gap3 = current.get(3).y - current.get(2).y;
            if (current.size() < 22 && Utility.validCombo(current,keywords)) { // && gap > 38 && gap2 > 38 && gap3 >38
                for (Combo c : current) {
                    //System.out.println(c);
                }
                //System.out.println("---------------------------------------");
                if (current.get(0).height > height || current.get(0).x < xvalue){
                    height = current.get(0).height;
                    xvalue = current.get(0).x;
                    output = new ArrayList<String>();
                    for (Combo c : current) {
                        output.add(c.text + "==" + c.url);
                    }
                }
            } 
        }
        return output;
    }
    
    public static ArrayList<String> getList(String url){
        ArrayList<String> output = new ArrayList<String>();
        try {
            Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("ul");
            for (Element list: result) {
                Elements items = list.select("li a");
                if (isValid(items, keywords)){
                    for (Element item: items){
                        output.add(item.text()+"=="+item.absUrl("href"));
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return output;
        }
        return output;
    }
    
   
    public static ArrayList<String> extract3(String url, ArrayList<Combo> original, int grouptype) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.arithmetic(combos, grouptype);
        ArrayList<ArrayList<Combo>> multiples = new ArrayList<ArrayList<Combo>>();
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);  
            if (current.size() >= 3 && current.size() < 22 && Utility.validCombo(current,keywords)) {
                multiples.add(current);
            } 
        }
        if (multiples.size() == 1){
            //System.out.println("only....");
            for (Combo c : multiples.get(0)) {
                output.add(c.text + "==" + c.url);
            }
        } else if (multiples.size() > 1) {
            //System.out.println("multi....");
            identifyOutput(multiples,output);
        }
        return output;
    }
    
    public static void identifyOutput(ArrayList<ArrayList<Combo>> multiples, ArrayList<String> output){
        boolean status = false;
        for (ArrayList<Combo> combos: multiples) {
            if (combos.get(1).text.toLowerCase().contains("school") || combos.get(1).text.toLowerCase().contains("college") || combos.get(1).text.toLowerCase().contains("division")) {
                status = true;
                for (Combo c: combos) {
                    output.add(c.text + "==" + c.url);
                }
            }
        }
        if (status == false) {
            for (ArrayList<Combo> combos: multiples) {
                for (Combo c : combos) {
                    output.add(c.text + "==" + c.url);
                }
            }
        }
    }
    
    
    public static ArrayList<String> extract(String url, ArrayList<Combo> original, int grouptype) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.tiled(combos, grouptype);
        HashMap<String,ArrayList<Combo>> tileds = new HashMap<String,ArrayList<Combo>>();
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() > 1 && current.size() < 12) {
                String index = current.get(0).y + "";
                if (tileds.keySet().contains(index)) {
                    tileds.get(index).addAll(current);
                    counts.put(index, counts.get(index)+1);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.addAll(current);
                    tileds.put(index, empty);
                    counts.put(index, 1);
                }
            } 
        }
        int height = 0;
        for (String key: tileds.keySet()) {
            ArrayList<Combo> current = tileds.get(key);
            if (current.size() < 3) continue;
            if (counts.get(key) > 1 && Utility.validCombo(current,keywords) && current.get(0).height > height) {
                height = current.get(0).height;
                output = new ArrayList<String>();
                for (Combo c : current) {
                    output.add(c.text + "==" + c.url);
                }
            }
        }
        return output;
    }
    
    public static ArrayList<String> extract(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        //for (Combo c: combos){
            //System.out.println(c);
        //}
        HashMap<String,ArrayList<Combo>> verticals =  Utility.tiled(combos);
        HashMap<String,ArrayList<Combo>> tileds = new HashMap<String,ArrayList<Combo>>();
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() > 1 && current.size() < 12) {
                String index = current.get(0).y + "";
                if (tileds.keySet().contains(index)) {
                    tileds.get(index).addAll(current);
                    counts.put(index, counts.get(index)+1);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.addAll(current);
                    tileds.put(index, empty);
                    counts.put(index, 1);
                }
            } 
        }
        int height = 0;
        for (String key: tileds.keySet()) {
            ArrayList<Combo> current = tileds.get(key);
            if (current.size() < 3) continue;
            if (counts.get(key) > 1 && Utility.validCombo(current,keywords) && current.get(0).height > height) {
                height = current.get(0).height;
                output = new ArrayList<String>();
                for (Combo c : current) {
                    output.add(c.text + "==" + c.url);
                }
            }
        }
        return output;
    }
    
    public static void resetCombo(ArrayList<Combo> combos, ArrayList<Combo> original){
        for (Combo c: original) {
            Combo brand = new Combo(c.x,c.y,c.text,c.tag,c.dom);
            brand.setFont(c.font);
            brand.setGroup(c.group);
            brand.setHeight(c.height);
            brand.setParent(c.parent);
            brand.setText(c.text);
            brand.setUrl(c.url);
            brand.setContract(c.contract);
            brand.previous = c.previous;
            combos.add(brand);
        }
    }
    
    public static ArrayList<String> extract2h(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> horizontals =  Utility.horizontal(combos);
        for (String key: horizontals.keySet()) {
            ArrayList<Combo> current = horizontals.get(key);
            if (current.size() < 3) continue;
            if (current.size() < 10 && Utility.validCombo(current,keywords)) {
                for (Combo c : current) {
                    output.add(c.text+"=="+c.url);
                }
                break;
            } 
        }
        return output;
    }
    
    public static ArrayList<String> getSchools(String url) throws IOException {
        ArrayList<Combo> combos = CSSModel.getCombos(url);
        ArrayList<String> iter0 = extract(url, combos); //第零轮：抓嵌套矩形列表
        if (iter0.size() >= 3) {
            //for (String str: iter0) System.out.println(str);
            ArrayList<String> d0 = SchoolNav.dedup(iter0, url);
            if (d0.size() >= 3) {
                System.out.println("第零轮：抓嵌套矩形列表");
                return d0;
            }
        }
        ArrayList<String> iter1 = extract2(url, combos); //第一轮：抓嵌套列表
        if (iter1.size() >= 3) {
            ArrayList<String> d1 = SchoolNav.dedup(iter1, url);
            if (d1.size() >= 3) {
                System.out.println("第一轮：抓嵌套列表");
                return d1;
            }
        }
        ArrayList<String> iter2 = extract(url, combos, 1);//第二轮：抓矩形列表，距离相似
        if (iter2.size() >= 3) {
            ArrayList<String> d2 = SchoolNav.dedup(iter2, url);
            if (d2.size() >= 3) {
                System.out.println("第二轮：抓矩形列表，距离相似");
                return d2;
            }
        }
        ArrayList<String> iter2s = extract(url, combos, 2); //第三轮：抓矩形列表，距离相等
        if (iter2s.size() >= 3) {
            ArrayList<String> d2s = SchoolNav.dedup(iter2s, url);
            if (d2s.size() >= 3) {
                System.out.println("第三轮：抓矩形列表，距离相等");
                return d2s;
            }
        }
        ArrayList<String> iter2h = extract2h(url, combos); //第四轮：抓横向列表
        if (iter2h.size() >= 3) {
            ArrayList<String> d2h = SchoolNav.dedup(iter2h, url);
            if (d2h.size() >= 3) {
                System.out.println("第四轮：抓横向列表");
                return d2h;
            }
        }
        ArrayList<String> iter3 = extract3(url, combos, 1); //第五轮：抓分组列表，距离相似
        if (iter3.size() >= 3) {
            ArrayList<String> d3 = SchoolNav.dedup(iter3, url);
            if (d3.size() >= 3) {
                System.out.println("第五轮：抓分组列表，距离相似");
                return d3;
            }
        }
        ArrayList<String> iter3s = extract3(url, combos, 2); //第六轮：抓分组列表，距离相等
        if (iter3s.size() >= 3) {
            ArrayList<String> d3s = SchoolNav.dedup(iter3s, url);
            if (d3s.size() >= 3) {
                System.out.println("第六轮：抓分组列表，距离相等");
                return d3s;
            }
        }
        return new ArrayList<String>();
    }
   
}
