package org.fit.cssbox.demo;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cz.vutbr.web.csskit.antlr.CSSParser.string_return;
import ch.qos.logback.core.joran.action.NewRuleAction;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class Faculty extends Thread {
	String dept_url = "";
        public static StringBuffer sb= new StringBuffer();
        public static int counter = 0;
	//public static HashMap<String, ArrayList<Person>> xs = new HashMap<String, ArrayList<Person>>();
	public  HashMap<String, ArrayList<Person>> xs = new HashMap<String, ArrayList<Person>>();
	public static HashMap<String, ArrayList<String>> Names = new HashMap<String, ArrayList<String>>();

	Faculty(String url) {
		this.dept_url = url;
	}

	public Faculty() {

	}
        public synchronized static void increment() {
            counter++;
        }

	public void getPage(String url) {
		ArrayList<Person> Persons = CSSModel.getPersons(url);
		for (Person m : Persons) {
			if (NameFilter(m.name)) {
				Person person = new Person(m.name, m.x, m.y, m.tag, m.width, m.heigth,
						m.parent);
				if (xs.get(person.tag + person.parent + person.x.toString()
						+ person.heigth.toString()) == null) // ��x�����ͬ���������һ��
				{
					ArrayList<Person> persons = new ArrayList<Person>();
					persons.add(person);
					xs.put(person.tag + person.parent + person.x.toString()
							+ person.heigth.toString(), persons);
				} else {
					xs.get(person.tag + person.parent + person.x.toString()
							+ person.heigth.toString()).add(person);
				}
			}
		}
	}

	public static boolean negative(String text) {
		//if(text.contains(s))
		//	return false;
		if (text.contains("Professor") || text.contains("Faculty")
				|| text.contains("Staff") || text.contains("Student")
				|| text.contains("Graduate") || text.contains("Undergraduate")) {
			return true;
		}
		return false;
	}

	public boolean NameFilter(String text)// �ж���ҳ�е�text�Ƿ�������ĸ�ʽ
	{
		// if(negative(text))
		// return false;
		if (text.length() == 0 || text.contains("@")
				|| text.toLowerCase().equals("web")
				|| text.toLowerCase().equals("website")
				|| text.toLowerCase().contains("web")
				|| text.toLowerCase().contains("group")
				|| text.toLowerCase().contains("research"))
			return false;
		if (text.contains(",")) {
			if (text.split(",").length > 4) {
				// System.out.println(text+" false 1");
				return false;
			} else {
				String[] parts = text.split(",");
				int num = 0;
				for (String part : parts) {
					part = part.trim();
					String[] blanks = part.trim().split(" ");
					num += blanks.length;
				}
				if (num >= 4) {
					// System.out.println(text+" false 2")
					return false;
				} else
					return true;
			}

		} else if (text.contains(" ")) {
			if (text.split(" ").length >= 4) {
				return false;
			} else
				return true;
		}
		return false;

	}

	public static void NameRead(String file)// ��ȡ������ݿ��е����
	{
		FileInputStream fstream = null;
		try {

			fstream = new FileInputStream(file);
			DataInputStream inflow = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(
					new InputStreamReader(inflow));
			String strLine;
			while ((strLine = br.readLine()) != null && !strLine.equals("")) {
				if (Names.get(((String) strLine.subSequence(0, 1))
						.toUpperCase()) == null) {
					ArrayList<String> alphbet = new ArrayList<String>();
					alphbet.add(strLine);
					Names.put(
							((String) strLine.subSequence(0, 1)).toUpperCase(),
							alphbet);
				} else {
					Names.get(
							((String) strLine.subSequence(0, 1)).toUpperCase())
							.add(strLine);
				}
			}
		} catch (IOException ex) {
		}

	}

	public ArrayList<Person> Duplication(ArrayList<Person> Namelist) 
	{
		ArrayList<Person> dedups = new ArrayList<Person>();
		ArrayList<String> tempAnchors = new ArrayList<String>();
		for (Person pson : Namelist) 
		{
			if (!tempAnchors.contains(pson.name)) 
			{
				dedups.add(pson);
				tempAnchors.add(pson.name);
			}
		}
		return dedups;
	}

	public boolean hasNumber(String s)
	{
	    boolean flag=false;	
	    String regex = "[^\\d]+";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(s);
		if(m.find())
		{
			if(m.group().equals(s))
			flag=false;
			else 
			flag=true;
		}
		else 
		{
			flag=true;
		}
		return flag;
	}
	public boolean NameListJudge(ArrayList<Person> Namelist)// �ж�ĳ���б��Ƿ�Ϊ�����б�
	{
		Namelist=Duplication(Namelist);
		
		boolean flag = false;
		if (Namelist.size() < 8)
			return false;
		int count = 0;
		//System.out.println("----------------------------");
		for (Person pson : Namelist) 
		{
			boolean isPersonName = false;
			System.out.print(pson.name + "==");
			String[] nas = pson.name.replaceAll("\\pP|\\pS", " ").split(" ");// �����еķ��ת���ɿո�,���ÿո�ͳһ��name�ִ�
			for (String na : nas)
			{
				na = na.toUpperCase();
				if(hasNumber(na))
					break;
				else if (na.equals("TO") || na.equals("AND") || na.equals("&")|| na.equals("THE") || na.equals("BUT")|| na.equals("FACULTY") || na.equals("STAFF")) 
					break;	
				else if (na.length() >= 2) 
				{
					ArrayList<String> res=new ArrayList<String>();
					res= Names.get(na.subSequence(0, 1));
					if (res != null) 
					{
						for (String re : res) 
						{
							if (na.equals(re.toUpperCase()))
							{
								System.out.println(re);
								count++;
								isPersonName = true;
								break;
							}
						}
						if (isPersonName)
							break;
					}
				}
			}
			System.out.println();
		}
		if (count > Namelist.size() * 3 / 5)
			flag = true;
		System.out.println(flag);
		System.out.println("=====================================");
		//System.out.println("----------------------------");
		return flag;
	}

	public boolean FacultyListJudge(String url)// �������е��б�
	{
		getPage(url);
		boolean isFacultyList = false;
		Set<Entry<String, ArrayList<Person>>> set = xs.entrySet();
		Iterator<Entry<String, ArrayList<Person>>> iter = set.iterator();
		while (iter.hasNext()) {
			Map.Entry<String, ArrayList<Person>> entry = (Map.Entry<String, ArrayList<Person>>) iter.next();
			String key = entry.getKey();
			ArrayList<Person> values = entry.getValue();
			if (NameListJudge(values)) {
				isFacultyList = true;
			}
			if (isFacultyList)
				break;
		}
		return isFacultyList;
	}

	public boolean inList(String link, ArrayList<String> linklist) {
		for (int i = 0; i < linklist.size(); i++)
		{
			if (link.equals(linklist.get(i)))
				return true;
		}
		return false;
	}

	public boolean hasKeyword (String text)
	{
		if(text.toLowerCase().contains("adjunct")||text.toLowerCase().contains("core")||text.toLowerCase().contains("faculty")||text.toLowerCase().contains("people")||text.toLowerCase().contains("staff")||text.toLowerCase().contains("alphabet")||text.toLowerCase().contains("directory"))
			return true;
		else 
			return false;
	}
	public ArrayList<String> getLink(String url, String keyword,ArrayList<String> linklist)
	 {
		ArrayList<String> links = new ArrayList<String>();
		if(url!=null&&(url.contains("http")||url.contains("www")))
		{
		String link = null;
		try {
			Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
			Elements result = doc.select("a");
			for (Element e : result)
			{
				String anchor = e.text().trim();
				if (anchor.isEmpty()) 
				{
					anchor = e.select("img").attr("alt");// text��ͼƬ����
				}
				String href = e.attr("abs:href").trim();
				if (!anchor.isEmpty() && anchor.split(" ").length <= 5&& !(anchor.toLowerCase().contains("find"))) {
					if (!anchor.toUpperCase().contains("PUBLICATION")&&!anchor.toUpperCase().contains("FACULTY SPOTLIGHT")&& anchor.toUpperCase().equals(keyword.toUpperCase())&& anchor.split(" ").length <5&& !href.equals("") && !href.contains("@")) 
					{
						if (!href.endsWith(".doc")&&!href.endsWith(".xls")&&!href.endsWith(".pdf")&&hasKeyword(href)&&!href.equals(url) && !inList(href, linklist)) 
						{
							link = href;
							links.add(link);
							// break;
						}
					} 
					else if (!anchor.toUpperCase().contains("PUBLICATION")&&!anchor.toUpperCase().contains("FACULTY SPOTLIGHT")&& anchor.toUpperCase().contains(keyword.toUpperCase())&& anchor.split(" ").length <5&& !href.equals("") && !href.contains("@"))
					{
						if (!href.endsWith(".doc")&&!href.endsWith(".xls")&&!href.endsWith(".pdf")&&hasKeyword(href)&&!href.equals(url) && !inList(href, linklist)) 
						{
							link = href;
							links.add(link);
							// break;
						}
					}
				}
			}

		} catch (IOException ex) {

		}
		}
		return links;
	}

	public String getFacultyListUrl(String deptUrl) {
		ArrayList<String> list1 = new ArrayList<String>();// �����һ��
		ArrayList<String> list2 = new ArrayList<String>();// ����ڶ���
		ArrayList<String> list3 = new ArrayList<String>();
		list1 = getLinks(deptUrl);
		for (int i = 0; i < list1.size(); i++) {
			list2.addAll(getLinks(list1.get(i)));
		}
		// list3.addAll(list1);//����ӵ�һ�� Ȼ����ӵڶ���

		for (int i = 0; i < list2.size(); i++) {
			boolean inList1 = false;
			for (int j = 0; j < list1.size(); j++) {
				if (list2.get(i).equals(list1.get(j))) {
					inList1 = true;
					break;
				}
			}
			if (!inList1)
				list3.add(list2.get(i));
		}
		list3.addAll(list1);// ����ӵڶ��� Ȼ����ӵ�һ��
		System.out.println("----------------------------------------");
		System.out.println("size=" + list3.size());
	    for (String url : list3) {
			System.out.println("link=" + url);
		}
		System.out.println("----------------------------------------");
		for (String url : list3) {
			System.out.println("=====================================");
			System.out.println("link=" + url);
			if (FacultyListJudge(url)) {
				return url;
			}
		}
		return " ";
	}

	public ArrayList<String> getLinks(String deptUrl) {
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> link = null;

		link = getLink(deptUrl, "Faculty", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "People", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Staff", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty & Staff Directory", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty AND Staff Directory", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty/Staff Directory", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty Directory", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty & Staff", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty listing", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty Profiles", list1);
		if (link != null)
			list1.addAll(link);
		link = getLink(deptUrl, "Faculty by name", list1);
		if (link != null)
			list1.addAll(link);
		return list1;
	}

	public static ArrayList<String> getURL(String file) {
		ArrayList<String> URLS = new ArrayList<String>();
		FileInputStream fstream = null;
		try {
			fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null && !strLine.equals("")) {
				URLS.add(strLine.split("==")[1]);
			}
		} catch (IOException ex) {

		}
		return URLS;
	}
        
        public void run() {
            String facurl = getFacultyListUrl(dept_url);
            sb.append(dept_url+" ("+facurl+")\n");
            if (facurl != null) increment();
        }

	
	public static void main(String[] args) throws IOException,
			InterruptedException, 
			BiffException {
                //Faculty fac = new Faculty();
		System.out.println("start");
		NameRead("Names.txt");
		//System.out.println(Names.size());
		//String url ="http://www.ices.cmu.edu/";
		//String facurl = fac.getFacultyListUrl(url);
		//System.out.println("facurl==" + facurl);
                
            Workbook workbook = Workbook.getWorkbook(new File("faculty200.xls"));
            Sheet sheet = workbook.getSheet(3);
            Faculty[] threads = new Faculty[sheet.getRows()];
            for (int i = 0; i < sheet.getRows(); i++) {
                String url = sheet.getCell(0, i).getContents().trim();
                threads[i] = new Faculty(url);
                threads[i].start();
            }
            for (int j = 0; j < threads.length; j++) {
                threads[j].join();
            }
            sb.append("Total: " + counter);
            FileUtils.writeStringToFile(new File("facultyNavExp.txt"), sb.toString());
		System.out.println("over");
	}

}
