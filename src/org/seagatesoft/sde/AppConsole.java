package org.seagatesoft.sde;

import java.io.*;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;

import org.seagatesoft.sde.columnaligner.ColumnAligner;
import org.seagatesoft.sde.columnaligner.PartialTreeAligner;
import org.seagatesoft.sde.datarecordsfinder.DataRecordsFinder;
import org.seagatesoft.sde.datarecordsfinder.MiningDataRecords;
import org.seagatesoft.sde.dataregionsfinder.DataRegionsFinder;
import org.seagatesoft.sde.dataregionsfinder.MiningDataRegions;
import org.seagatesoft.sde.tagtreebuilder.DOMParserTagTreeBuilder;
import org.seagatesoft.sde.tagtreebuilder.TagTreeBuilder;
import org.seagatesoft.sde.treematcher.EnhancedSimpleTreeMatching;
import org.seagatesoft.sde.treematcher.SimpleTreeMatching;
import org.seagatesoft.sde.treematcher.TreeMatcher;
import org.xml.sax.SAXException;

/**
 * Aplikasi utama yang berbasis konsol.
 * 
 * @author seagate
 *
 */
public class AppConsole implements Runnable
{
    public Formatter output;
    public static String namesFile = "Group/Names.txt";
    public static double similarityTreshold = 0.8; //default: 0.80
    public static boolean ignoreFormattingTags = false; //default: false
    public static boolean useContentSimilarity = false; //default: false
    public int maxNodeInGeneralizedNodes = 1; //default: 9
    public static boolean humanConsumable = true;
    public static boolean intermediateResult = false;
    public static List<String> remaining = Collections.synchronizedList(new ArrayList<String>());
    public String name;
    public String url;
    public static boolean onServer = true;
    public static String filename = "001";
    public static boolean production = true;
    public String directoryName;

    public AppConsole(String name, String url, int nodes) throws FileNotFoundException {
        this.name = name;
        this.url = url;
        this.maxNodeInGeneralizedNodes = nodes;
        String resultOutput = "26CSFaculty/" + name + ".html";
        this.output = new Formatter(new File(resultOutput));
    }
    
    public AppConsole(String directoryName) {
        this.directoryName = directoryName;
    }
    
    public static List<String> getKeywords(String file) throws FileNotFoundException, IOException {
        List<String> keywords = new ArrayList<String>();
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null && !strLine.trim().equals("")) {
            keywords.add(strLine.trim());
        }
        return keywords;
    }

    public static void main(String args[]) throws IOException, InterruptedException {
        if (production) {
            int cpus = Runtime.getRuntime().availableProcessors();
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cpus);
            File[] files = new File("Directory").listFiles();
            for (File file: files) {
                Runnable task = new AppConsole(file.getName());
                executor.execute(task);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                System.out.println("Status: " + executor.getCompletedTaskCount() + "/" + executor.getTaskCount() + " threads are completed ...");
                Thread.sleep(10000);
            }
            System.out.println("Finished all threads...");
        } else {
            if (onServer) {
                String inputFile = "Group/TestFaculty.txt";
                List<String> lines = getKeywords(inputFile);
                int cpus = Runtime.getRuntime().availableProcessors();
                ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cpus);
                for (int r = 0; r < lines.size(); r++) {
                    String filename = r + 1 + "";
                    if (r + 1 < 10) {
                        filename = "00" + filename;
                    } else if (r + 1 < 100) {
                        filename = "0" + filename;
                    }
                    Runnable task = new AppConsole(filename, lines.get(r), 1);
                    executor.execute(task);
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                    System.out.println("Status: " + executor.getCompletedTaskCount() + "/" + executor.getTaskCount() + " threads are completed ...");
                    Thread.sleep(10000);
                }
                System.out.println("First iteration done...");
                if (remaining.size() != 0) {
                    executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cpus);
                    for (String name : remaining) {
                        int index = Integer.parseInt(name) - 1;
                        Runnable task = new AppConsole(name, lines.get(index), 2);
                        executor.execute(task);
                    }
                    executor.shutdown();
                    while (!executor.isTerminated()) {
                        System.out.println("Status: " + executor.getCompletedTaskCount() + "/" + executor.getTaskCount() + " threads are completed ...");
                        Thread.sleep(10000);
                    }
                }
                System.out.println("Finished all threads");
            } else {
                String inputFile = "Group/TestFaculty.txt";
                List<String> lines = getKeywords(inputFile);
                new AppConsole(filename, lines.get(Integer.parseInt(filename) - 1), 1).process();
                System.out.println("Done...");
            }
        }
    }
    
    @Override
    public void run() {
        processUniv();
    }
    
    public void processUniv() {
        try {
            System.out.println("Start processing: "+directoryName);
            List<String> rows = getKeywords("Directory/"+directoryName);
            String basePath = "96 Faculty/"+directoryName.split("\\.")[0];
            new File(basePath).mkdir();
            if (rows.size() == 0) return;
            for (int i=0; i<rows.size(); i++) {
                this.maxNodeInGeneralizedNodes = 1;
                List<String[][]> tables = getTables(rows.get(i));
                if (tables == null) {
                    this.maxNodeInGeneralizedNodes = 2;
                    tables = getTables(rows.get(i));
                }
                if (tables != null) {
                    generatePerson(tables, basePath, i);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AppConsole.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AppConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void generatePerson(List<String[][]> tables, String basePath, int index) {
        StringBuilder sb = new StringBuilder();
        for (String[][] table: tables) {
            for (String[] row: table) {
                if (row[0] != null && !row[0].equals("")) {
                    sb.append("photo=="+row[0]+"\n");
                }
                if (row[1] != null && !row[1].equals("")) {
                    sb.append("name=="+row[1]+"\n");
                }
                if (row[2] != null && !row[2].equals("")) {
                    sb.append("web=="+row[2]+"\n");
                }
                if (row[3] != null && !row[3].equals("")) {
                    sb.append("position=="+row[3]+"\n");
                }
                if (row[4] != null && !row[4].equals("")) {
                    sb.append("email=="+row[4]+"\n");
                }
                if (row[5] != null && !row[5].equals("")) {
                    sb.append("phone=="+row[5]+"\n");
                }
                sb.append("\n");
            }
        }
        try {
            FileUtils.writeStringToFile(new File(basePath+"/"+index+".txt"), sb.toString());
        } catch (IOException ex) {
            Logger.getLogger(AppConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public List<String[][]> getTables(String listUrl) {
        try {
            TagTreeBuilder builder = new DOMParserTagTreeBuilder();
            TagTree tagTree = builder.buildTagTree(listUrl, ignoreFormattingTags);
            TreeMatcher matcher = new SimpleTreeMatching();
            DataRegionsFinder dataRegionsFinder = new MiningDataRegions(matcher);
            List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarityTreshold);
            DataRecordsFinder dataRecordsFinder = new MiningDataRecords(matcher);
            DataRecord[][] dataRecords = new DataRecord[dataRegions.size()][];

            for (int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++) {
                DataRegion dataRegion = dataRegions.get(dataRecordArrayCounter);
                dataRecords[ dataRecordArrayCounter] = dataRecordsFinder.findDataRecords(dataRegion, similarityTreshold);
            }

            ColumnAligner aligner = null;
            if (useContentSimilarity) {
                aligner = new PartialTreeAligner(new EnhancedSimpleTreeMatching());
            } else {
                aligner = new PartialTreeAligner(matcher);
            }
            List<String[][]> dataTables = new ArrayList<String[][]>();
            List<String[][]> tempTables = new ArrayList<String[][]>();

            for (int tableCounter = 0; tableCounter < dataRecords.length; tableCounter++) {
                String[][] dataTable = aligner.alignDataRecords(dataRecords[tableCounter]);

                if (dataTable != null && FacultyList.identify(dataTable, intermediateResult)) {
                    //truncate(dataTable);
                    tempTables.add(dataTable);
                }
            }

            for (String[][] tempTable : tempTables) {
                String[][] formatted = formatTable(tempTable);
                if (formatted != null && formatted.length > 0) {
                    dataTables.add(formatted);
                }
            }
            if (dataTables.size() != 0) {
                return dataTables;
            } else return null;
        } catch (IOException ex) {
            Logger.getLogger(AppConsole.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(AppConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void process() {
        System.out.println("Start processing: " + url);
        try {
            TagTreeBuilder builder = new DOMParserTagTreeBuilder();
            TagTree tagTree = builder.buildTagTree(url, ignoreFormattingTags);
            TreeMatcher matcher = new SimpleTreeMatching();
            DataRegionsFinder dataRegionsFinder = new MiningDataRegions(matcher);
            List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarityTreshold);
            DataRecordsFinder dataRecordsFinder = new MiningDataRecords(matcher);
            DataRecord[][] dataRecords = new DataRecord[dataRegions.size()][];

            for (int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++) {
                DataRegion dataRegion = dataRegions.get(dataRecordArrayCounter);
                dataRecords[ dataRecordArrayCounter] = dataRecordsFinder.findDataRecords(dataRegion, similarityTreshold);
            }

            ColumnAligner aligner = null;
            if (useContentSimilarity) {
                aligner = new PartialTreeAligner(new EnhancedSimpleTreeMatching());
            } else {
                aligner = new PartialTreeAligner(matcher);
            }
            List<String[][]> dataTables = new ArrayList<String[][]>();
            List<String[][]> tempTables = new ArrayList<String[][]>();

            for (int tableCounter = 0; tableCounter < dataRecords.length; tableCounter++) {
                String[][] dataTable = aligner.alignDataRecords(dataRecords[tableCounter]);

                if (dataTable != null && FacultyList.identify(dataTable, intermediateResult)) {
                    //truncate(dataTable);
                    tempTables.add(dataTable);
                }
            }
            if (humanConsumable) {
                for (String[][] tempTable : tempTables) {
                    String[][] formatted = formatTable(tempTable);
                    if (formatted != null && formatted.length > 0) {
                        dataTables.add(formatted);
                    } 
                }
            } else {
                dataTables = tempTables;
            }
            if (dataTables.size()==0) remaining.add(name);
            output.format("<html><head><title>Extraction Result</title>");
            output.format("<style type=\"text/css\">table {border-collapse: collapse;} td {padding: 5px} table, th, td { border: 3px solid black;} </style>");
            output.format("</head><body>");
            int tableCounter = 1;
            output.format("<a href=\"%s\">Faculty Page</a>\n\n", url);
            for (String[][] table : dataTables) {
                output.format("<h2>Table %s</h2>\n<table>\n<thead>\n<tr>\n<th>Row Number</th>\n", tableCounter);
                for (int columnCounter = 1; columnCounter <= table[0].length; columnCounter++) {
                    output.format("<th></th>\n");
                }
                output.format("</tr>\n</thead>\n<tbody>\n");
                int rowCounter = 1;
                for (String[] row : table) {
                    output.format("<tr>\n<td>%s</td>", rowCounter);
                    for (String item : row) {
                        String itemText = item;
                        if (itemText == null) {
                            itemText = "";
                        }
                        output.format("<td>%s</td>\n", itemText.replaceAll(" ", " ")); //remove special space
                    }
                    output.format("</tr>\n");
                    rowCounter++;
                }
                output.format("</tbody>\n</table>\n");
                tableCounter++;
            }
            output.format("</body></html>");
        } catch (SecurityException exception) {
            System.out.println("Exception caused by "+url);
            exception.printStackTrace();
            //return;
        } catch (FileNotFoundException exception) {
            System.out.println("Exception caused by "+url);
            exception.printStackTrace();
            //return;
        } catch (IOException exception) {
            System.out.println("Exception caused by "+url);
            exception.printStackTrace();
            //return;
        } catch (SAXException exception) {
            System.out.println("Exception caused by "+url);
            exception.printStackTrace();
            //return;
        } catch (Exception exception) {
            System.out.println("Exception caused by "+url);
            exception.printStackTrace();
            //return;
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
        
    private static String[][] formatTable(String[][] tempTable) {
        if (tempTable==null || tempTable.length==0 || tempTable[0].length==0) return null;
        boolean name = false;
        boolean web = false;
        boolean photo = false;
        boolean email = false;
        boolean phone = false;
        boolean position = false;
        int cols = tempTable[0].length;
        String[][] result = new String[tempTable.length][6];
        for (int i=0; i<cols; i++) {
            if (name && web && photo && email && phone && position) break;
            if (!photo && FacultyList.photoColumn(tempTable,i)) {
                FacultyList.addPhoto(result, tempTable, i);
                photo = true;
            }
            if (!name && FacultyList.nameColumn(tempTable, i)) {
                FacultyList.addName(result, tempTable, i);
                name = true;
            }
            if (!web && FacultyList.webColumn(tempTable, i)){
                FacultyList.addWeb(result, tempTable, i);
                web = true;
            }
            if (!position && FacultyList.positionColumn(tempTable, i)){
                FacultyList.addPosition(result, tempTable, i);
                position = true;
            }
            if (!email && FacultyList.emailColumn(tempTable, i)) {
                FacultyList.addEmail(result, tempTable, i);
                email = true;
            }
            if (!phone && FacultyList.phoneColumn(tempTable, i)){
                FacultyList.addPhone(result, tempTable, i);
                phone = true;
            }
        }
        return result;
    }

}