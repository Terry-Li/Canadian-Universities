/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Yifeng
 */
public class ListEngine {
    private ArrayList<String> positives;
    private ArrayList<String> negatives;
    private ArrayList<String> degrees;

    public ListEngine(ArrayList<String> positives, ArrayList<String> negatives, ArrayList<String> degrees) {
        this.positives = positives;
        this.negatives = negatives;
        this.degrees = degrees;
    }
    
    public boolean negative(String text,String url) {
        if (!text.matches("\\D*")) {
            return true;
        }
        for (String neg: negatives) {
            if (neg.length()>5 && text.toLowerCase().contains(neg.toLowerCase())) {
                return true;
            } else if (neg.length()<=5) {
                String[] tokens = text.split(" ");
                for (String token: tokens) {
                    if (token.equalsIgnoreCase(neg)){
                        return true;
                    }
                }
            }
        }
        if (url != null){
            for (String neg : negatives) {
                if (neg.length() > 5 && url.toLowerCase().contains(neg.toLowerCase())) {
                    return true;
                } 
            }
        }
        for (String degree: degrees) {
            if (text.contains(degree) || (url!=null && url.contains(degree))) {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(ArrayList<String> keywords, String text) {
        String[] tokens = text.split(" ");
        for (String keyword: keywords) {
            if (keyword.length() > 5 && text.toUpperCase().contains(keyword.toUpperCase())){
                return true;
            } else {
                for (String token : tokens) {
                    if (token.replaceAll(",", "").equalsIgnoreCase(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean containNames(ArrayList<String> keywords, String text) {
        String[] tokens;
        if (text.contains(",")) {
            tokens = text.split(", ");
        } else {
            tokens = text.split(" ");
        }
        for (String keyword: keywords) {
            for (String token: tokens) {
                if (keyword.trim().equalsIgnoreCase(token.trim())) {
                    //System.out.println(keyword);
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean validNameCombo(ArrayList<Combo> combos) {
        int count = 0;
        for (Combo c: combos) {
            if (c.text.split(" ").length <9 && containNames(positives,c.text) && !negative(c.text,c.url)) {
                count++;
            }
        }
        if (count >= (float)combos.size()*0.67) {
            return true;
        } else return false;
    }
    
    public ArrayList<String> getNames(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.verticalNames(combos);
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() < 3) continue;
            if (validNameCombo(current)) {
                for (Combo c : current) {
                    //System.out.println(c.text);
                    if (c.url != null && !c.url.startsWith("http")) {
                        output.add(c.text + "==" + new URL(new URL(url), c.url).toString());
                    } else {
                        output.add(c.text + "==" + c.url);
                    }
                }
                //break;
                //System.out.println("=========================================");
            }
        }
        return SchoolNav.dedup(output, url);
    }
    
    public ArrayList<Integer> getNameCombos(String url, ArrayList<Combo> original) throws IOException {
        ArrayList<Integer> output = new ArrayList<Integer>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        for (int i=0;i<combos.size();i++) {
            combos.get(i).index = i;
        }
        HashMap<String,ArrayList<Combo>> verticals =  Utility.verticalNames(combos);
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() < 3) continue;
            //for (Combo c: current) {
                //System.out.println(c.text);
            //}
            //System.out.println("-----------------------------");
            if (validNameCombo(current)) {
                for (Combo c : current) {
                    output.add(c.index);
                }
            }
        }
        return output;
    }
    
    public boolean validCombo(ArrayList<Combo> combos) {
        int count = 0;
        for (Combo c: combos) {
            if (c.text.split(" ").length <9 && contains(positives,c.text) && !negative(c.text,c.url)) {
                count++;
                //System.out.println(c.text);
            }
        }
        //System.out.println("-----------------------------");
        if (count >= (float)combos.size()*0.67) {
            return true;
        } else return false;
    }
    
    public void resetCombo(ArrayList<Combo> combos, ArrayList<Combo> original){
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
            brand.style = c.style;
            brand.title = c.title;
            combos.add(brand);
        }
    }
    
    public SemanticList vertical(Link link, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.vertical(combos);
        int height = 0;
        int xvalue = 0;
        boolean needHead = true;
        String head = null;
        String title = null;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() < 3) continue;
            if (validCombo(current)) {
                //for (Combo c: current) System.out.println(c);
                if (current.get(0).height > height || current.get(0).x < xvalue){
                    height = current.get(0).height;
                    xvalue = current.get(0).x;
                    output = new ArrayList<String>();
                    needHead = true;
                    for (Combo c : current) {
                        if (needHead) {
                            needHead = false;
                            title = c.title;
                            if (c.previous != null) {
                                head = c.previous.text;
                            }
                        }
                        if (c.url == null || c.url.startsWith("http")) {
                            output.add(c.text + "==" + c.url);
                        } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                            output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                        }
                    }
                }
            } 
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    public SemanticList horizontal(Link link, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> horizontals =  Utility.horizontal(combos);
        int height = 0;
        int yvalue = 0;
        boolean needHead = true;
        String head = null;
        String title = null;
        for (String key: horizontals.keySet()) {
            ArrayList<Combo> current = horizontals.get(key);
            if (current.size() < 3) continue;
            if (validCombo(current)) {
                if (current.get(0).height > height || current.get(0).y < yvalue){
                    height = current.get(0).height;
                    yvalue = current.get(0).y;
                    output = new ArrayList<String>();
                    needHead = true;
                    for (Combo c : current) {
                        if (needHead) {
                            needHead = false;
                            title = c.title;
                            if (c.previous != null) {
                                head = c.previous.text;
                            }
                        }
                        if (c.url == null || c.url.startsWith("http")) {
                            output.add(c.text + "==" + c.url);
                        } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                            output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                        }
                    }
                }
            } 
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    
    public SemanticList nestedVertical(Link link, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.vertical(combos);
        int height = 0;
        int xvalue = 10000;
        boolean needHead = true;
        String head = null;
        String title = null;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() < 3) continue;
            float total = 0;
            for (int i = 0; i < current.size() - 1; i++) {
                total += current.get(i + 1).y - current.get(i).y - current.get(i).contract;
            }
            float gap = total / (current.size() - 1);
            if (gap > 50 && validCombo(current)) {
                if (current.get(0).height > height || current.get(0).x < xvalue){
                    height = current.get(0).height;
                    xvalue = current.get(0).x;
                    output = new ArrayList<String>();
                    needHead = true;
                    for (Combo c : current) {
                        if (needHead) {
                            needHead = false;
                            title = c.title;
                            if (c.previous != null) {
                                head = c.previous.text;
                            }
                        }
                        if (c.url == null || c.url.startsWith("http")) {
                            output.add(c.text + "==" + c.url);
                        } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                            output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                        }
                    }
                } 
            } 
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    public SemanticList nestedTiled(Link link, ArrayList<Combo> original) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.tiled(combos);
        HashMap<String,ArrayList<Combo>> tileds = new HashMap<String,ArrayList<Combo>>();
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
        boolean needHead = true;
        String head = null;
        String title = null;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            if (current.size() > 1) {
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
            if (counts.get(key) > 1 && validCombo(current) && current.get(0).height > height) {
                height = current.get(0).height;
                output = new ArrayList<String>();
                needHead = true;
                for (Combo c : current) {
                    if (needHead) {
                        needHead = false;
                        title = c.title;
                        if (c.previous != null) {
                            head = c.previous.text;
                        }
                    }
                    if (c.url == null || c.url.startsWith("http")) {
                        output.add(c.text + "==" + c.url);
                    } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")){
                        output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                    }
                }
            }
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    public SemanticList tiled(Link link, ArrayList<Combo> original, int grouptype) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.tiled(combos, grouptype);
        for (Combo c: combos){
            if (c.x == 509) {
                //System.out.println(c);
            }
        }
        HashMap<String,ArrayList<Combo>> tileds = new HashMap<String,ArrayList<Combo>>();
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
        boolean needHead = true;
        String head = null;
        String title = null;
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);
            for (Combo c: current) {
                //System.out.println(c);
            }
            //System.out.println("----------------------------");
            if (current.size() >= 1) {
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
            if (counts.get(key) > 1 && validCombo(current) && current.get(0).height > height) {
                height = current.get(0).height;
                output = new ArrayList<String>();
                needHead = true;
                for (Combo c : current) {
                    //System.out.println(c);
                    if (needHead) {
                        needHead = false;
                        title = c.title;
                        if (c.previous != null) {
                            head = c.previous.text;
                        }
                    }
                    //System.out.println("----------------"+c.url+"--------------------");
                    if (c.url == null || c.url.startsWith("http")) {
                        output.add(c.text + "==" + c.url);
                    } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")){
                        output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                    } 
                }
                //System.out.println("----------------------------");
            }
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    public SemanticList indexed(Link link, ArrayList<Combo> original, int grouptype) throws IOException {
        ArrayList<String> output = new ArrayList<String>();
        ArrayList<Combo> combos = new ArrayList<Combo>();
        resetCombo(combos, original);
        HashMap<String,ArrayList<Combo>> verticals =  Utility.arithmetic(combos, grouptype);
        boolean needHead = true;
        String head = null;
        String title = null;
        ArrayList<ArrayList<Combo>> candidates = new ArrayList<ArrayList<Combo>>();
        for (String key: verticals.keySet()) {
            ArrayList<Combo> current = verticals.get(key);  
            if (current.size() >= 3 && validCombo(current)) {
                candidates.add(current);
            } 
        }
        if (candidates.size()==1) {
            for (Combo c: candidates.get(0)) {
                if (needHead) {
                    needHead = false;
                    title = c.title;
                    if (c.previous != null) {
                        head = c.previous.text;
                    }
                }

                if (c.url == null || c.url.startsWith("http")) {
                    output.add(c.text + "==" + c.url);
                } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                    output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                }
            }
        } else if (candidates.size() > 1) {
            boolean isConsistent = true;
            for (int i=1;i<candidates.size();i++) {
                if (candidates.get(i).get(0).height!=candidates.get(i-1).get(0).height || !candidates.get(i).get(0).style.equals(candidates.get(i-1).get(0).style)){
                    isConsistent = false;
                    break;
                }
            }
            if (isConsistent) {
                for (int j=0;j<candidates.size();j++) {
                    for (Combo c : candidates.get(j)) {
                        if (needHead) {
                            needHead = false;
                            title = c.title;
                            if (c.previous != null) {
                                head = c.previous.text;
                            }
                        }

                        if (c.url == null || c.url.startsWith("http")) {
                            output.add(c.text + "==" + c.url);
                        } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                            output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                        }
                    }
                }
            } else {
                boolean success = false;
                for (ArrayList<Combo> candidate: candidates) {
                    Combo previous = candidate.get(0).previous;
                    if (previous!=null && (previous.text.toLowerCase().contains("schools")||previous.text.toLowerCase().contains("colleges"))) {
                        for (Combo c : candidate) {
                            if (needHead) {
                                needHead = false;
                                title = c.title;
                                if (c.previous != null) {
                                    head = c.previous.text;
                                }
                            }

                            if (c.url == null || c.url.startsWith("http")) {
                                output.add(c.text + "==" + c.url);
                            } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                                output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                            }
                        }
                        success = true;
                        break;
                    }
                }
                if (!success) {
                    for (Combo c : candidates.get(0)) {
                        if (needHead) {
                            needHead = false;
                            title = c.title;
                            if (c.previous != null) {
                                head = c.previous.text;
                            }
                        }

                        if (c.url == null || c.url.startsWith("http")) {
                            output.add(c.text + "==" + c.url);
                        } else if (!c.url.contains("://") && !c.url.toLowerCase().contains("javascript")) {
                            output.add(c.text + "==" + new URL(new URL(link.url), c.url).toString());
                        }
                    }
                }
            }
        }
        output = SchoolNav.dedup(output, link.url);
        if (output.size() < 3) {
            return null;
        } else {
            SemanticList list = new SemanticList();
            list.context = link;
            list.head = head;
            list.title = title;
            list.list = output;
            return list;
        }
    }
    
    public static boolean hasSchoolKeyword(SemanticList list) {
        int count = 0;
        for (String str: list.list) {
            String anchor = str.toLowerCase().split("==")[0];
            if (anchor.contains("school") || anchor.contains("college")){
                count++;
            }
        }
        return count*2>list.list.size();
    }
    
    public SemanticList getSchools(Link link) throws IOException {
        //System.out.println("Why:...");
        ArrayList<Combo> combos = CSSModel.getCombos(link.url);
        //System.out.println(combos.size());
        for (Combo c: combos) {
            if (true) {
                //System.out.println(c);
                //System.out.println(c.style);
            }
        }
        ArrayList<SemanticList> candidates = new ArrayList<SemanticList>();
        SemanticList iter0 = nestedTiled(link, combos); //第零轮：抓嵌套矩形列表
        if (iter0 != null) {
            System.out.println("第零轮：抓嵌套矩形列表");
            candidates.add(iter0);
        }
        SemanticList iter1 = nestedVertical(link, combos); //第一轮：抓嵌套列表
        if (iter1 != null) {
            System.out.println("第一轮：抓嵌套列表");
            candidates.add(iter1);
        }
        SemanticList iter2 = tiled(link, combos, 1);//第二轮：抓矩形列表，距离相似
        if (iter2 != null) {
            System.out.println("第二轮：抓矩形列表，距离相似");
            candidates.add(iter2);
        }
        
        SemanticList iter3 = tiled(link, combos, 2); //第三轮：抓矩形列表，距离相等
        if (iter3 != null) {
            System.out.println("第三轮：抓矩形列表，距离相等");
            candidates.add(iter3);
        }
        SemanticList iter4 = vertical(link, combos); //第四轮：抓垂直列表
        if (iter4 != null) {
            System.out.println("第四轮：抓垂直列表");
            candidates.add(iter4);
        }
        SemanticList iter5 = horizontal(link, combos); //第五轮：抓横向列表
        if (iter5 != null) {
            System.out.println("第五轮：抓横向列表");
            candidates.add(iter5);
        }
        SemanticList iter6 = indexed(link, combos, 1); //第六轮：抓分组列表，距离相似
        if (iter6 != null) {
            System.out.println("第六轮：抓分组列表，距离相似");
            candidates.add(iter6);
        }
        SemanticList iter7 = indexed(link, combos, 2); //第七轮：抓分组列表，距离相等
        if (iter7 != null) {
            System.out.println("第七轮：抓分组列表，距离相等");
            candidates.add(iter7);
        }
        if (candidates.size()==0) {
            return null;
        } else {
            for (int i=0;i<candidates.size();i++) {
                if (hasSchoolKeyword(candidates.get(i))) {
                    System.out.println(i);
                    return candidates.get(i);
                }
            }
            return candidates.get(0);
        }
    }
    
}
