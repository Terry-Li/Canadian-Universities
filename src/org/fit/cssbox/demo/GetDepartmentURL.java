package org.fit.cssbox.demo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 给定系名，在指定页面中找出该名对应的URL
 * 
 * @author Administrator
 * 
 */
public class GetDepartmentURL {
    
   
	public static class URL {
		public String url = null; // 统一默认初始化为null
		public String nodeTxt = null; // 该URL对应的节点值
	}
	
	public static class Dept {
		public String deptName = null; // 学院名，不包含'department of'
		public String deptAbbreviation = null; // 学院名缩写
	}
	
	public Document pageDoc = null; // 包含学院的页面URL
	public String deptName = null; // 学院名
	
	public static void main(String[] args) throws IOException {
		GetDepartmentURL departmentUrl = new GetDepartmentURL();
                Document doc = Jsoup.connect("http://ualr.edu/academics/colleges-and-departments/").timeout(12000).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
		String url = departmentUrl.getDepartmentURL(doc,"College of Professional Studies");
		System.out.println(url);
	}
	
	/**
	 * 根据学院名，取该学院名缩写和不包含'department of'等带'of'的名字
	 * 返回的结果中，全部转为大写
	 * 
	 * @param deptName
	 * @return
	 */
	public Dept tansformDept(String deptName) {
		if(deptName == null || "".equals(deptName)) {
			return null;
		}
		
		Dept dept = new Dept();
		// 去掉'of'及'of'前的所有字符
		dept.deptName = deptName.toUpperCase().replaceAll(".*\\s+of\\s+", "");
		// 缩写
		dept.deptAbbreviation = getAbbreviation(dept.deptName);
		
		return dept;
	}
	
	/**
	 * 取给定名字的缩写（即名字中各单词的首字母）
	 * 
	 * @param name
	 * @return
	 */
	public String getAbbreviation(String name) {
		// 去掉特殊字符，但不能去空格，后续需要通过空格分隔单词
		name = name.replaceAll("[^a-zA-Z\\s]+", "");
		// 去掉'and' 'of'
		name = name.replaceAll("and|of", "");
		String[] words = name.split("\\s+");
		StringBuffer sb = new StringBuffer();
		for(String word : words) {
			if(word.length() > 0) {
				sb.append(word.charAt(0));
			}
		}
		
		return sb.toString().toUpperCase();
	}
	
	/**
	 * 在指定页面中，找出指定系名的URL
	 * 
	 * @param deptName 指定系名
	 * @param pageUrl 指定页面的URL
	 * @return 该系名对应的URL
	 */
	public String getDepartmentURL(Document doc, String name) {
                pageDoc = doc;
                deptName = name;
		// 取出正确的节点
		Element rightElement = getRightElement(deptName, pageDoc);
		// 取出该节点的URL，或者兄弟节点的URL，或者上级节点的兄弟节点的URL
		List<URL> urls = getUrls(rightElement);
		// 帅选URL，根据关键字、<a>的值是否为图片
		URL url = filterUrls(urls);
		
		if(url == null) {
			return null;
		}
		// 返回正确的URL
		return url.url;
	}
	
	/**
	 * 从给定的URL列表中，帅选出正确的URL
	 * 
	 * @param urls
	 * @return
	 */
	public URL filterUrls(List<URL> urls) {
		URL rightUrl = null;
		if(urls == null) {
			return null;
		}
		for(URL url : urls) {
                        if (url.url.contains("#") || url.url.toLowerCase().contains("building") || 
                                url.url.toLowerCase().contains("@")) continue;
			// 包含关键字
			if(url.nodeTxt.toUpperCase().contains("DEPARTMENT WEBSITE") ||
					url.nodeTxt.toUpperCase().contains("SCHOOL WEBSITE") ||
					url.nodeTxt.toUpperCase().contains("DEPARTMENT HOME") ||
					url.nodeTxt.toUpperCase().contains("HOME") ||
					url.nodeTxt.toUpperCase().contains("WEBSITE") ||
					url.nodeTxt.toUpperCase().contains("WEB SITE") ||
                                        url.nodeTxt.toUpperCase().contains("COLLEGE HOME PAGE") ||
                                        url.nodeTxt.toUpperCase().contains("HTTP")) {
				return url;
			}
			// 包含学院名或学院名缩写
			Dept dept = tansformDept(deptName);
			if(url.nodeTxt.toUpperCase().contains(dept.deptName) || url.nodeTxt.toUpperCase().contains(dept.deptAbbreviation) || url.url.toUpperCase().contains(dept.deptAbbreviation)) {
				return url;
			}
		}
		// 如果没有符合要求的URL，则取URL列表中的第一个
		rightUrl = urls.size() > 0 && !urls.get(0).url.contains("#") ? urls.get(0) : null;
		
		return rightUrl;
	}
	
	/**
	 * 根据给定的学院名，获取正确的节点
	 * 
	 * @param deptName
	 * @param pageUrl
	 * @return
	 */
	public Element getRightElement(final String deptName, final Document doc) {
		//Document doc = getDocFromUrl(pageUrl);
		Element rightElement = null;
		if(doc != null) {
			Elements elems = doc.getAllElements();
			for(Element e : elems) {
				String nodeText = e.ownText();
                                int links = e.getElementsByTag("a").size();
				if(!e.tagName().equals("a") && links == 0 && deptName.equals(nodeText.trim())) {
					rightElement = e;
					break;
				}
			}
		}
		
		return rightElement;
	}
	
	/**
	 * 计算兄弟节点中，节点名和本节点名相同的个数(不包含自己)
	 * 
	 * @param siblingElements
	 * @param nodeName
	 * @return
	 */
	public int calSiblingSizeWithSameName(Element element) {
		int siblingSize = 0;
		String nodeName = element.nodeName();
		System.out.println("node name: " + nodeName);
		for(Element e : element.siblingElements()) {
			if(nodeName.equals(e.nodeName())) {
				siblingSize++;
				System.out.println(e.text());
			}
		}
		return siblingSize;
	}
	
	/**
	 * 给定页面的URL，返回Jsoup的Document元素
	 * 
	 * @param url 给定页面的URL
	 * @return 返回Jsoup的Document元素
	 */
        /*
	public Document getDocFromUrl(final String pageUrl) {
		Document doc = null;
		try {
			doc = Jsoup.connect(pageUrl).timeout(60 * 1000).get();
			return doc;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	} */
	
	/**
	 * 获得该元素下的所有URL，找到即返回
	 * 如果该元素下没有URL，则查找该元素的兄弟节点，直到找到URL，找到即返回
	 * 如果所有兄弟节点都没有URL，则查找上级节点的兄弟节点下的URL，找到即返回
	 * 还没有找到，直接返回
	 * 
	 * @param ele
	 * @return
	 */
	public List<URL> getUrls(final Element ele) {
		if(ele == null) {
			return null;
		}
		List<URL> lUrls = null;
		lUrls = getUrlsOfElement(ele);
		if(lUrls.size() > 0) { // 找URL，则立即返回
			return lUrls;
		}
		// 该节点及所有子节点下没有URL，则继续查找兄弟节点
		Element siblingElement = ele.nextElementSibling();
		while(siblingElement != null) { // 有兄弟节点
			lUrls = getUrlsOfElement(siblingElement); // 获取兄弟节点的URL及所有子节点的URL
			if(lUrls.size() > 0) {
				return lUrls;
			}
			siblingElement = siblingElement.nextElementSibling();
		}
		
		// 兄弟节点中还没有找到，则找上级节点(简单起见，直接找该上级节点下的所有URL)
		Element parent = ele.parent();
		lUrls = getParentUrls(parent);
		
		return lUrls;
	}
	
	/**
	 * 取上级节点的所有子节点URL，如果没有，取上上级节点，直到取到URL为止
	 * 
	 * @param element
	 * @return
	 */
	public List<URL> getParentUrls(Element element) {
		List<URL> lUrls = null;
		Element parent = element.parent();
		lUrls = getUrlsOfElement(parent);
		if(lUrls.size() == 0) {
			lUrls = getUrlsOfElement(element.parent());
		}
		
		return lUrls;
	}
	
	/**
	 * 获取某个元素下的所有URL，包含该元素的子节点，以及子子节点...
	 * 
	 * @param ele
	 * @return
	 */
	public List<URL> getUrlsOfElement(final Element ele) {
		List<URL> urls = new ArrayList<URL>();
		Elements elements = ele.children();
		for(Element element : elements) {
			concatList(urls, getUrlsOfElement(element));
		}
		String url = ele.attr("abs:href"); // 取该节点的URL
		String nodeText = ele.ownText(); // 取该节点的值
		if(!"".equals(url) && !"".equals(nodeText)) {// && !url.toLowerCase().contains("students")&& !url.toLowerCase().contains("undergraduate")
			URL cUrl = new URL();
			cUrl.url = url;
			cUrl.nodeTxt = nodeText;
			urls.add(cUrl);
		}
		
		return urls;
	}
	
	/**
	 * 合并List，将list2合并到list1中
	 * 
	 * @param list1
	 * @param list2
	 */
	public void concatList(List<URL> list1, final List<URL> list2) {
		if(list1 == null || list2 == null ) {
			return;
		}
		for(URL val : list2) {
			list1.add(val);
		}
	}
        
        public static boolean similar(String anchor1, String anchor2) {
            if (anchor1.equalsIgnoreCase("") || anchor2.equalsIgnoreCase("")) return false;
            if (anchor1.equalsIgnoreCase("research") || anchor2.equalsIgnoreCase("research")) return false;
            if (anchor1.contains(anchor2)||anchor2.contains(anchor1)) {
                return true;
            }
            return false;
        }
        
        public static ArrayList<String> parallelLinks(String url, int count){
            ArrayList<String> links = new ArrayList<String>();
            try {
                ArrayList<Combo> combos = CSSModel.getCombos(url);
                for (Combo c: combos){
                    //if (c.text.toLowerCase().contains("visit"))System.out.println(c);
                }
                HashMap<String,ArrayList<Combo>> maps = Utility.vertical(combos);
                for (String key: maps.keySet()) {
                    ArrayList<Combo> current = maps.get(key);
                    //if (current.size() == 15) {
                        for (Combo c : current) {
                            //System.out.println(c.text);
                        }
                        //System.out.println("-----------------------");
                    //}
                    //System.out.println(count);
                    if (current.size() == count && current.get(0).url != null) {
                        for (Combo c: current){
                            links.add(c.url);
                        }
                        break;
                    } 
                }
          
            } catch (MalformedURLException ex) {
                Logger.getLogger(GetDepartmentURL.class.getName()).log(Level.SEVERE, null, ex);
                return links;
            }
            return links;
        }
	
}
