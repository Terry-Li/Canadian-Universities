/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Terry
 */
public class Engine extends Thread {
    String univName;
    String univUrl;

    public Engine(String univName, String univUrl) {
        this.univName = univName;
        this.univUrl = univUrl;
    }
    
    public void run(){
        try {
            ArrayList<String> schools = Sort.getSchools(univUrl);
            StringBuilder sb = new StringBuilder();
            if (schools.size()>=4) {
                sb.append(univUrl+"\n");
                for (String school:schools) {
                    sb.append(school+"\n");
                }
                FileUtils.writeStringToFile(new File("Univs2\\"+univName+".txt"), sb.toString());
            }
        } catch (IOException ex) {
            Logger.getLogger(Engine.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException, InterruptedException {
        ArrayList<String> urls = Utility.getKeywords("newlist.txt");
        Engine[] threads = new Engine[urls.size()];
        for (int i=0;i<urls.size();i++) {
            threads[i] = new Engine(""+i,urls.get(i).split("==")[2]);
            threads[i].start();
        }
        for (Engine thread: threads) {
            thread.join();
        }
    }
}
