package Zion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.fit.cssbox.demo.*;

/**
 *
 * @author Terry
 */
public class SUSE implements Runnable{
    public String univName;
    public String univURL;
    public static String dataCenter = "C:/Users/admin/Desktop/Canadian Universities/Data Center/";
    public static final String[] gateway = {"academics","academic units","schools","colleges","divisions", "faculties",
        "departments","department list","programs","faculty","directory","people","staff"};
    public Set<String> visited = new HashSet<String>();


    public SUSE(String univName, String univURL) {
        this.univName = univName;
        this.univURL = univURL;
    }
    
    public void run() {
        process();
    }
    
    public void process(){
        try {
            Link univLink = new Link();
            univLink.url = univURL;
            univLink.context = new ArrayList<String>();
            System.out.println("Processing "+univName+"...");
            SemanticList schoolList = SchoolNav.getSchoolsResult(univLink,visited);
            StringBuilder sb = new StringBuilder();
            sb.append(univName+"=="+univURL+"\n");
            if (schoolList != null) {   
                System.out.println("Success!");
                ArrayList<String> schools = schoolList.list;
                for (String school:schools) {
                    //String schoolFac = FacultyNav.getFacultyURL(school.split("==")[1], visited);
                    sb.append("School=="+school+"\n");  
                    /*
                    String schoolURL = school.split("==")[1];
                    if (!schoolURL.equals("null")) {
                        Link schoolLink = new Link();
                        schoolLink.url = schoolURL;
                        SemanticList deptList = DepartmentNav.getDeptsResult(schoolLink, visited, schools);
                        if (deptList != null) {
                            ArrayList<String> departments = deptList.list;
                            for (String dept: departments) {
                                String deptFac = FacultyNav.getFacultyURL(dept.split("==")[1], visited);
                                sb.append("Dept=="+dept+"("+deptFac+")\n");
                            }
                        }
                    } */               
                    //sb.append("\n");
                }
                
            }
            FileUtils.writeStringToFile(new File(dataCenter+univName+".txt"), sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(SUSE.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
     
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> lines = FileUtils.readLines(new File("../Input/Elite96.txt"));
        ExecutorService executor = Executors.newFixedThreadPool(6);
        for (int i=0; i<lines.size(); i++) {
            Runnable task = new SUSE(i+1+"",lines.get(i).split("==")[1]);
            executor.execute(task);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");
    }
}