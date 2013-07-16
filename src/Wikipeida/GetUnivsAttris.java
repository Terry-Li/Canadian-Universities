package Wikipeida;

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class GetUnivsAttris {
	/**
	 * @param args
	 * 通过大学名称在wikipedia上获得大学的属性
	 */

	//根据大学名称得到大学在wikipedia上的属性
	public static ArrayList<String> getUnivsArrributes(String univName){
		ArrayList<String> attris = new ArrayList<String>();
		univName = univName.replaceAll(" ", "_");
		String univUrl = "http://en.wikipedia.org/wiki/" + univName;  //大学在wikipedia上的网址
		System.out.println("univWikeUrl:" + univUrl);
		Elements attriLinks = getAttriLinks(univUrl);
		if(!attriLinks.isEmpty()){
			attris = getAttris(attriLinks); //取得大学的属性，保存的结构为attriName#attriValue结构
		}
		
		return attris;
	}
	//获得一个大学的所有属性的链接
	public static Elements getAttriLinks(String url){
		Elements trs = new Elements();
		try {
			Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
			int size = doc.select("table").size();
			Element table = doc.select("table").get(0);
			int i = 1;
			while(!isAttribute(table.text()) && i<size){   //找到属性存在的那个table
				table = doc.select("table").get(i);
				i++;
			}
			if(!isAttribute(table.text()) && i>=size){
				table = null;
				return trs;
			}
			trs = table.select("tr");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return trs;
	}
	//是否是属性所在的table，由于几乎所有的大学属性中都有Motto这个属性，可通过是否有它来判断是否找到正确的table还是根本没有内容
	public static boolean isAttribute(String txt){
		boolean isattri = false;
		String regex = "Motto";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(txt);
		if(matcher.find())
			isattri = true;
		else{
			String[] regexs = new String[3];
			regexs[0] = "Established";
			regexs[1] = "Location";
			regexs[2] = "Website";
			for(int i=0;i<regexs.length;i++){
				Pattern pattern1 = Pattern.compile(regexs[i]);
				Matcher matcher1 = pattern1.matcher(txt);
				if(matcher1.find())
					isattri = true;
				else{
					isattri = false;
					break;
				}
			}
		}
		return isattri;
	}
	//获得属性名称和属性值，保存到链表中，保存的内容的形式为attriName1#attriValue1...
	public static ArrayList<String> getAttris(Elements trs){
		ArrayList<String> attris = new ArrayList<String>();
		Element th = trs.get(0).select("th").get(0);    //第一tr标签间的内容是大学
		String univName = th.text();
		univName = stressTransform(univName);
//		attris.add("UnivName" + "#" + univName);
		for(int i=1;i<trs.size();i++){   //接下来的tr标签中，有<th>和<td>的标签存在的内容是属性名和属性值
			if(trs.get(i).select("th").size() == 1 && trs.get(i).select("td").size() == 1){
				String attriName = trs.get(i).select("th").get(0).text().trim();    
				String attriValue = trs.get(i).select("td").get(0).text().trim();
				attriName = stressTransform(attriName);
				attriValue = stressTransform(attriValue);
				attriValue = delQuote(attriValue);   //去掉头尾的引号
		/*		if(attriName.equals("Website") && attriValue.equals("[1]"))   //这里的Website是一个连接，可以通过读<a href=" ">中href的内容得到Website的值
				{                                                              //否则，经过下面的去掉方括号的处理后，Website的内容就变成了空值，出现错误
					Element td = trs.get(i).select("td").get(0);
					Element tagA = td.getElementsByTag("a").get(0);
					attriValue = tagA.attr("href");
				}*/
				//处理attriName
				attriName = mergeAttriName(attriName);   //合并一些名称类似的属性名
				attriName = dealAttriName(attriName);
				//处理attriValue
				attriValue = deleSquareBracket(attriValue);	
				if(attriName.equals("Location")){
					attriValue = delLocationCoordinate(attriValue);
				}
				attriValue = changeSpecialBlank(attriValue);
				attriValue = delMoreBlank(attriValue);
				if(!attriName.isEmpty() && !attriValue.isEmpty())
					attris.add(attriName + "#" + attriValue);
			}
		}
		
		return attris;
	}
	//去掉属性中的引号
	public static String delQuote(String attriValue){
		if(attriValue.contains("\""))
			attriValue = attriValue.replaceAll("\"", "");
		if(attriValue.contains("\'"))
			attriValue = attriValue.replaceAll("\'", "");
		
		return attriValue;
	}
	//处理得到的属性名称
	public static String dealAttriName(String attriName){
		if(attriName.contains("."))
			attriName = attriName.replaceAll("\\.", "");
		if(attriName.contains(" "))
			attriName = attriName.replaceAll(" ", "_");
		
		return attriName;
	}
	
	//合并同类型的属性名
	public static String mergeAttriName(String attriName){
		if(attriName.equals("Sport Teams") || attriName.equals("Sports team") || attriName.equals("Sports Team"))
			attriName = "Sports teams";
		if(attriName.equals("Colors"))
			attriName = "Colours";
		
		return attriName;
	}
	
	//去掉得到的属性值中的“[2]”的信息
	public static String deleSquareBracket(String attriValue){
		String regex = "\\[\\d+\\](\\[[a-zA-Z]\\s\\d\\])?";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(attriValue);
		if(matcher.find()){
			attriValue = matcher.replaceAll("");
		}
		
		return attriValue;
	}
	//去掉多余的空格
	public static String delMoreBlank(String attriValue){
		String newAttriValue = "";
		StringTokenizer token = new StringTokenizer(attriValue);
		while(token.hasMoreTokens()){
			if(newAttriValue.isEmpty())
				newAttriValue = token.nextToken();
			else
				newAttriValue += " " + token.nextToken();
		}

		return newAttriValue;
	}
	
	//去掉多余的空格
	public static String changeSpecialBlank(String attriValue){
		int n = 160;
		char c = (char)n;
		int length = attriValue.length();
		int i = 0;
		
		while(i<length){
			if(attriValue.charAt(i) == c)
			{
				attriValue = attriValue.replace(c, ' ');
			}
			i++;
		}

		return attriValue;
	}
	
	//去掉Location属性中的坐标内容,留下了一个坐标
	public static String delLocationCoordinate(String locationValue){
		StringTokenizer token = new StringTokenizer(locationValue,"/");
		if(token.hasMoreTokens())
		{
			locationValue = token.nextToken();
		}
		
		return locationValue;
	}
    //将有音标的单词转化为正常英语
    public static String stressTransform(String str){
    	if(str.contains("à") || str.contains("á") || str.contains("â") || str.contains("ã") || str.contains("ä") || str.contains("å")){
    		str = str.replaceAll("à", "a");
    		str = str.replaceAll("á", "a");
    		str = str.replaceAll("â", "a");
    		str = str.replaceAll("ã", "a");
    		str = str.replaceAll("ä", "a");
    		str = str.replaceAll("å", "a");
    	}
    	if(str.contains("À") || str.contains("Á") || str.contains("Â") || str.contains("Ã") || str.contains("Ä") || str.contains("Å")){
    		str = str.replaceAll("À", "A");
    		str = str.replaceAll("Á", "A");
    		str = str.replaceAll("Â", "A");
    		str = str.replaceAll("Ã", "A");
    		str = str.replaceAll("Ä", "A");
    		str = str.replaceAll("Å", "A");
    	}
    	if(str.contains("é") || str.contains("è") || str.contains("ë") || str.contains("ê")){
    		str = str.replaceAll("é", "e");
    		str = str.replaceAll("è", "e");
    		str = str.replaceAll("ë", "e");
    		str = str.replaceAll("ê", "e");
    	}	
    	if(str.contains("È") || str.contains("É") || str.contains("Ê") || str.contains("Ë")){
    		str = str.replaceAll("È", "E");
    		str = str.replaceAll("É", "E");
    		str = str.replaceAll("Ê", "E");
    		str = str.replaceAll("Ë", "E");
    	}
    	if(str.contains("æ") || str.contains("Æ") || str.contains("œ")){
    		str = str.replaceAll("æ", "ae");
    		str = str.replaceAll("Æ", "AE");
    		str = str.replaceAll("œ", "oe");
    	}
    	if(str.contains("ù") || str.contains("ú") || str.contains("û") || str.contains("ü")){
    		str = str.replaceAll("ù", "u");
    		str = str.replaceAll("ú", "u");
    		str = str.replaceAll("û", "u");
    		str = str.replaceAll("ü", "u");
    	}
    	if(str.contains("Ù") || str.contains("Ú") || str.contains("Û") || str.contains("Ü")){
    		str = str.replaceAll("Ù", "U");
    		str = str.replaceAll("Ú", "U");
    		str = str.replaceAll("Û", "U");
    		str = str.replaceAll("Ü", "U");
    	}
    	if(str.contains("ì") || str.contains("í") || str.contains("î") || str.contains("ï")){
    		str = str.replaceAll("ì", "i");
    		str = str.replaceAll("í", "i");
    		str = str.replaceAll("î", "i");
    		str = str.replaceAll("ï", "i");
    	}
    	if(str.contains("Ì") || str.contains("Í") || str.contains("Î") || str.contains("Ï")){
    		str = str.replaceAll("Ì", "I");
    		str = str.replaceAll("Í", "I");
    		str = str.replaceAll("Î", "I");
    		str = str.replaceAll("Ï", "I");
    	}
    	if(str.contains("ò") || str.contains("ó") || str.contains("ô") || str.contains("õ") || str.contains("ö") || str.contains("ø")){
    		str = str.replaceAll("ò", "o");
    		str = str.replaceAll("ó", "o");
    		str = str.replaceAll("ô", "o");
    		str = str.replaceAll("õ", "o");
    		str = str.replaceAll("ö", "o");
    		str = str.replaceAll("ø", "o");
    	}
    	if(str.contains("Ò") || str.contains("Ó") || str.contains("Ô") || str.contains("Õ") || str.contains("Ö") || str.contains("Ø")){
    		str = str.replaceAll("Ò", "O");
    		str = str.replaceAll("Ó", "O");
    		str = str.replaceAll("Ô", "O");
    		str = str.replaceAll("Õ", "O");
    		str = str.replaceAll("Ö", "O");
    		str = str.replaceAll("Ø", "O");
    	}
    	if(str.contains("Ç") || str.contains("Ð") || str.contains("Ñ") || str.contains("Ý") || str.contains("Þ") || str.contains("ß") || str.contains("ç") || str.contains("ð") || str.contains("ñ") || str.contains("ý") || str.contains("þ") || str.contains("ÿ")){
    		str = str.replaceAll("Ç", "C");
    		str = str.replaceAll("Ð", "eth");
    		str = str.replaceAll("Ñ", "N");
    		str = str.replaceAll("Ý", "Y");
    		str = str.replaceAll("Þ", "THORN");
    		str = str.replaceAll("ß", "s");
    		str = str.replaceAll("ç", "c");
    		str = str.replaceAll("ð", "eth");
    		str = str.replaceAll("ñ", "n");
    		str = str.replaceAll("ý", "y");
    		str = str.replaceAll("þ", "thorn");
    		str = str.replaceAll("ÿ", "y");
    	}
    	if(str.contains("’") || str.contains("–") || str.contains("s̜")){
    		str = str.replaceAll("’", "'");
    		str = str.replaceAll("–", "-");
    		str = str.replaceAll("s̜", "s");
    	}
    	
    	return str;
    }
}
