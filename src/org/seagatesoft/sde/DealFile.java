package org.seagatesoft.sde;


import java.io.*;
import java.util.ArrayList;

public class DealFile {

	public static ArrayList<String> readFile(String filename){
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(filename));
			String line = null;
			while((line = reader.readLine()) != null && !line.equals("")){
				lines.add(line);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return lines;
	}
	
	public static void writeFile(ArrayList<String> repeats,String filename){
		for(int i=0;i<repeats.size();i++){
			try {
				FileOutputStream fs_out = new FileOutputStream(filename,true);
				DataOutputStream out = new DataOutputStream(fs_out);
				out.writeBytes(repeats.get(i));
				out.writeBytes("\r\n");
				out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	
	public static void writeFile1(ArrayList<ArrayList<String>> repeats,String filename){
		for(int i=0;i<repeats.size();i++){
			try {
				FileOutputStream fs_out = new FileOutputStream(filename,true);
				DataOutputStream out = new DataOutputStream(fs_out);
				for(int j=0;j<repeats.get(i).size();j++){
					out.writeBytes(repeats.get(i).get(j));
					out.writeBytes("\r\n");
				}
				out.writeBytes("\r\n");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	}
	
}
