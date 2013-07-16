/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Terry
 */
public class Stats {
    public static String dataCenter = "C:\\Users\\Terry\\Desktop\\DataCenter\\outputFile";
    public static void schools() throws FileNotFoundException, IOException {
        int schoolCount = 0;
        int deptCount = 0;
        int nullSchool = 0;
        int nullDept = 0;
        Set<String> schoolNulls = new TreeSet<String>();
        StringBuilder schoolSb = new StringBuilder();
        StringBuilder deptSb = new StringBuilder();
        File[] files = new File(dataCenter).listFiles();
        int numUniv = 0;
        for (File university: files) {
            String univName = university.getAbsolutePath();
            File file = new File(univName+"\\School.txt");
            if (file.exists()) {
                numUniv++;
                String schoolText = FileUtils.readFileToString(file);
                String[] schools = schoolText.split("\r\n\r\n");
                //System.out.println(schoolText);
                //System.out.println("-----------------------------------------------------------");
                for (String school: schools) {
                    String[] depts = school.split("\r\n");
                    deptCount = deptCount + depts.length - 1;
                    if (depts.length > 1 && depts[1].split("==").length==3 && depts[1].split("==")[2].equals("null")) {
                        deptSb.append(depts[0]+"\n");
                        nullDept++;
                    } else if (depts[0].split("==")[2].equals("null")) {
                        //schoolSb.append(university.getName()+"\n");
                        schoolNulls.add(university.getName());
                    }
                }
                schoolCount = schoolCount+ schools.length;
            }
        }
        nullSchool = schoolNulls.size();
        for (String uni: schoolNulls) {
            schoolSb.append(uni+"\n");
        }
        System.out.println(numUniv);
        System.out.println("Schools: "+schoolCount+"  Departments: "+deptCount+" NullSchools: "+nullSchool+"  NullDepartments: "+nullDept);
        FileUtils.writeStringToFile(new File("SchoolNulls.txt"), schoolSb.toString());
        FileUtils.writeStringToFile(new File("DepartmentNulls.txt"), deptSb.toString());
    }
    
    public static void lists() throws FileNotFoundException, IOException {
        File[] files = new File("Faculty").listFiles();
        int list = 0;
        for (File f: files) {
            ArrayList<String> tokens = Utility.getKeywords("Faculty//"+f.getName());
            list += tokens.size();
        }
        System.out.println("Total lists: "+list);
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        schools();
    }
}
