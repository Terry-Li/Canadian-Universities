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
public class Sort2 {
    public static ArrayList<String> keywords;
    static {
        try {
            keywords = Utility.getKeywords("departments.txt");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Sort.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ArrayList<String> extractSpecial(String url, ArrayList<Combo> original) {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.vertical(combos);
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() == 3 && (current.get(0).text.toLowerCase().startsWith("department of") || current.get(0).text.toLowerCase().endsWith("department"))
                    && (current.get(1).text.toLowerCase().startsWith("department of") || current.get(1).text.toLowerCase().endsWith("department"))
                    && (current.get(2).text.toLowerCase().startsWith("department of") || current.get(2).text.toLowerCase().endsWith("department"))) {
                for (Combo c : current) {
                    output.add(c.text + "==" + c.url);
                }
                break;
            }
        }
        if (output.size()==0) {
            for (String key: verticals.keySet()) {
                ArrayList<Combo> current = verticals.get(key);
                if (current.size()==3 && current.get(0).previous !=null && current.get(0).previous.text.equalsIgnoreCase("departments")) {
                    for (Combo c : current) {
                        output.add(c.text + "==" + c.url);
                    }
                    break;
                }
            }
        }
        return output;
    }
    
    public static ArrayList<String> extract2(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.vertical(combos);
        int height = 0;
        int xvalue = 10000;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            //for (Combo c: current) System.out.println(c);
            //System.out.println("---------------------");
            if (current.size() < 4) continue;
            int gap = current.get(1).y - current.get(0).y - current.get(0).contract;
            int gap2 = current.get(2).y - current.get(1).y - current.get(1).contract;
            int gap3 = current.get(3).y - current.get(2).y - current.get(2).contract; 
            if (current.size() < 60 && Utility.validCombo2(current,keywords)) { // && gap > 40 && gap2 > 40 && gap3 >40
                if (current.get(0).height > height || current.get(0).x < xvalue){
                    height = current.get(0).height;
                    xvalue = current.get(0).x;
                    output = new ArrayList<String>();
                    for (Combo c : current) {
                        output.add(c.text + "==" + c.url);
                        //System.out.println(c);
                    }
                    //System.out.println("---------------------");
                } 
            } 
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
            //for (Combo c: current) System.out.println(c);
            //System.out.println("------------------------------"+grouptype);
            if (current.size() >= 3 && current.size() < 30 && Utility.validCombo2(current,keywords)) {
                multiples.add(current);
                //for (Combo c: current) System.out.println(c);
                //System.out.println("------------------------------"+grouptype);
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
            if (combos.get(0).previous.text.toLowerCase().contains("departments") || combos.get(1).text.toLowerCase().contains("department") || combos.get(1).text.toLowerCase().contains("school") || combos.get(1).text.toLowerCase().contains("division")) {
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
                    //System.out.println(c.text);
                }
                //System.out.println("------------------------");
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
            if (current.size() >= 1 && current.size() < 12) {
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
            if (counts.get(key) > 1 && Utility.validCombo2(current,keywords) && current.get(0).height > height) {
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
            if (counts.get(key) > 1 && Utility.validCombo2(current,keywords) && current.get(0).height > height) {
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
            for (Combo c: current) {
                //System.out.println(c.text);
            }
            //System.out.println("------------------------------");
            if (current.size() >= 3 && current.size() < 10 && Utility.validCombo2(current,keywords)) {
                for (Combo c : current) {
                    output.add(c.text+"=="+c.url);
                    //System.out.println(c.text);
                }
                break;
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
    
    public static ArrayList<String> getDepts(String url) throws IOException {
        
        ArrayList<Combo> combos = CSSModel.getCombos(url);
        //System.out.println("0");
        ArrayList<String> iter0 = extract(url, combos); //第零轮：抓嵌套矩形列表
        if (iter0.size() >= 3) {
            ArrayList<String> d0 = SchoolNav.dedup(iter0, url);
            if (Utility.isValidResult(d0)) {
                System.out.println("第零轮：抓嵌套矩形列表");
                return d0;
            }
        }
        //System.out.println("1");
        ArrayList<String> iter1 = extract2(url, combos); //第一轮：抓嵌套列表
        
        if (iter1.size() >= 3) {
            ArrayList<String> d1 = SchoolNav.dedup(iter1, url);
            
            if (Utility.isValidResult(d1)) {
                System.out.println("第一轮：抓嵌套列表");
                return d1;
            }
        }
        //System.out.println("2");
        ArrayList<String> iter2 = extract(url, combos, 1);//第二轮：抓矩形列表，距离相似
        if (iter2.size() >= 3) {
            ArrayList<String> d2 = SchoolNav.dedup(iter2, url);
            if (Utility.isValidResult(d2)) {
                System.out.println("第二轮：抓矩形列表，距离相似");
                return d2;
            }
        }
        //System.out.println("3");
        ArrayList<String> iter2s = extract(url, combos, 2); //第三轮：抓矩形列表，距离相等
        if (iter2s.size() >= 3) {
            ArrayList<String> d2s = SchoolNav.dedup(iter2s, url);
            if (Utility.isValidResult(d2s)) {
                System.out.println("第三轮：抓矩形列表，距离相等");
                return d2s;
            }
        }
        //System.out.println("4");
        ArrayList<String> iter2h = extract2h(url, combos); //第四轮：抓横向列表
        if (iter2h.size() >= 3) {
            ArrayList<String> d2h = SchoolNav.dedup(iter2h, url);
            if (Utility.isValidResult(d2h)) {
                System.out.println("第四轮：抓横向列表");
                return d2h;
            }
        }
        //System.out.println("5");
        ArrayList<String> iter3 = extract3(url, combos, 1); //第五轮：抓分组列表，距离相似
        if (iter3.size() >= 3) {
            ArrayList<String> d3 = SchoolNav.dedup(iter3, url);
            if (Utility.isValidResult(d3)) {
                System.out.println("第五轮：抓分组列表，距离相似");
                return d3;
            }
        }
        //System.out.println("6");
        ArrayList<String> iter3s = extract3(url, combos, 2); //第六轮：抓分组列表，距离相等
        if (iter3s.size() >= 3) {
            ArrayList<String> d3s = SchoolNav.dedup(iter3s, url);
            if (Utility.isValidResult(d3s)) {
                System.out.println("第六轮：抓分组列表，距离相等");
                return d3s;
            }
        }

        ArrayList<String> special = extractSpecial(url, combos);
        if (special.size() >= 3) {
            ArrayList<String> dspecial = SchoolNav.dedup(special, url);
            if (Utility.isValidResult(dspecial)) {
                System.out.println("第七轮：特殊列表");
                return dspecial;
            }

        }
        return new ArrayList<String>();
    }
   
}
