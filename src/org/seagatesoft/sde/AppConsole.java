package org.seagatesoft.sde;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    public static double similarityTreshold = 0.6; //default: 0.80
    public static boolean ignoreFormattingTags = false; //default: false
    public static boolean useContentSimilarity = false; //default: false
    public static int maxNodeInGeneralizedNodes = 1; //default: 9
    public static boolean humanConsumable = false;
    public static boolean intermediateResult = false;
    public static List<String> remaining = Collections.synchronizedList(new ArrayList<String>());
    public String name;
    public String url;

    public AppConsole(String name, String url) throws FileNotFoundException {
        this.name = name;
        this.url = url;
        String resultOutput = "26CSFaculty/" + name + ".html";
        this.output = new Formatter(new File(resultOutput));
    }

    public static void main(String args[]) throws IOException {
        //AppConsole app = new AppConsole("085","http://cidse.engineering.asu.edu/facultyandresearc/director/faculty/");
        //app.process();
        
        String inputFile = "Group/TestFaculty.txt";
        List<String> lines = FileUtils.readLines(new File(inputFile));
        ExecutorService executor = Executors.newFixedThreadPool(20);
        for (int r = 0; r < lines.size(); r++) {
            String filename = r+1+"";
            if (r+1 < 10) {
                filename = "00"+filename;
            } else if (r+1 < 100) {
                filename = "0"+filename;
            }
            Runnable task = new AppConsole(filename,lines.get(r));
            executor.execute(task);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        if (remaining.size() != 0) {
            executor = Executors.newFixedThreadPool(10);
            maxNodeInGeneralizedNodes = 2;
            for (String name: remaining) {
                int index = Integer.parseInt(name)-1;
                Runnable task = new AppConsole(name,lines.get(index));
                executor.execute(task);
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
        }
        System.out.println("Finished all threads");
    }
    
    @Override
    public void run() {
        process();
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
                    } else {
                        formatted = formatTable2(tempTable);
                        if (formatted != null && formatted.length > 0) {
                            dataTables.add(formatted);
                        }
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
            exception.printStackTrace();
            //System.exit(1);
        } catch (FileNotFoundException exception) {
            exception.printStackTrace();
            //System.exit(2);
        } catch (IOException exception) {
            exception.printStackTrace();
            //System.exit(3);
        } catch (SAXException exception) {
            exception.printStackTrace();
            //System.exit(4);
        } catch (Exception exception) {
            exception.printStackTrace();
            //System.exit(5);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
    
    private static void truncate(String[][] table) {
        for (int row=0;row<table.length;row++) {
            List<Integer> links = new ArrayList<Integer>();
            List<Integer> texts = new ArrayList<Integer>();
            for (int col=0;col<table[0].length;col++) {
                if (table[row][col] != null && table[row][col].contains("Link&lt;&lt;")) {
                    links.add(col);
                } else {
                    texts.add(col);
                }
            }
            for (Integer text: texts) {
                if (table[row][text]==null) continue;
                for (Integer link: links) {
                    if (table[row][text]==null) break;
                    if (table[row][link].split("a>").length == 2) {
                        table[row][text] = table[row][text].replace(table[row][link].split("a>")[1].trim(), "").trim();
                    }
                    if (table[row][text].equals("")) {
                        table[row][text] = null;
                    }
                }
            }
        }
    } 
        
    private static String[][] formatTable(String[][] tempTable) {
        List<Integer> photos = new ArrayList<Integer>();
        List<Integer> names = new ArrayList<Integer>();
        for (int col = 0; col < tempTable[0].length; col++) {
            if (FacultyList.photoColumn(tempTable, col)) {
                photos.add(col);
            }
            if (FacultyList.nameColumn(tempTable, col)) {
                names.add(col);
            }
        }
        System.out.println(photos.size() + "/" + names.size());
        if (photos.size() == names.size()) {
            List<String[]> tempList = new ArrayList<String[]>();
            for (int row = 0; row < tempTable.length; row++) {
                for (int i = 0; i < names.size(); i++) {
                    String nameCell = tempTable[row][names.get(i)];
                    String photoCell = tempTable[row][photos.get(i)];
                    if (nameCell != null) {
                        String[] pair = new String[3];
                        Pattern p = Pattern.compile("href=\"(.*)\".*</a>(.*)", Pattern.DOTALL);
                        //System.out.println(nameCell);
                        Matcher match = p.matcher(nameCell);
                        if (match.find()) {
                            pair[1] = match.group(2);
                            pair[2] = match.group(1);
                        }
                        pair[0] = photoCell;
                        if (!pair[1].equals("")) {
                            tempList.add(pair);
                        }
                    }
                }
            }
            String[][] result = new String[tempList.size()][];
            for (int i = 0; i < result.length; i++) {
                result[i] = tempList.get(i);
            }
            return result;
        } else if (photos.size() == 0) {
            List<String[]> tempList = new ArrayList<String[]>();
            for (int row = 0; row < tempTable.length; row++) {
                for (Integer col : names) {
                    String cell = tempTable[row][col];
                    if (cell != null) {
                        String[] pair = new String[2];
                        Pattern p = Pattern.compile("href=\"(.*)\".*</a>(.*)");
                        Matcher match = p.matcher(cell);
                        if (match.find()) {
                            pair[0] = match.group(2);
                            pair[1] = match.group(1);
                        }
                        if (!pair[0].equals("")) {
                            tempList.add(pair);
                        }
                    }
                }
            }
            String[][] result = new String[tempList.size()][];
            for (int i = 0; i < result.length; i++) {
                result[i] = tempList.get(i);
            }
            return result;
        } else {
            return null;
        }
    }
    
    private static String[][] formatTable2(String[][] tempTable) {
        List<Integer> photos = new ArrayList<Integer>();
        List<Integer> names = new ArrayList<Integer>();
        List<Integer> webs = new ArrayList<Integer>();
        for (int col = 0; col < tempTable[0].length; col++) {
            if (FacultyList.photoColumn(tempTable, col)) {
                photos.add(col);
            }
            if (FacultyList.nameColumn2(tempTable, col)) {
                names.add(col);
            }
            if (FacultyList.webColumn(tempTable, col)) {
                webs.add(col);
            }
        }
        System.out.println(photos.size() + "/" + names.size()+ "/" + webs.size());
        if (photos.size() == names.size()) {
            List<String[]> tempList = new ArrayList<String[]>();
            for (int row = 0; row < tempTable.length; row++) {
                for (int i = 0; i < names.size(); i++) {
                    String nameCell = tempTable[row][names.get(i)];
                    String photoCell = tempTable[row][photos.get(i)];
                    String webCell = null;
                    if (names.size()==webs.size()) {
                        webCell = tempTable[row][webs.get(i)];
                    } else if (names.size()*2==webs.size()) {
                        webCell = tempTable[row][webs.get(i*2)];
                    }
                    if (nameCell != null) {
                        String[] pair = new String[3];
                        pair[0] = photoCell;
                        pair[1] = nameCell;
                        pair[2] = webCell;
                        if (!pair[1].equals("")) {
                            tempList.add(pair);
                        }
                    }
                }
            }
            String[][] result = new String[tempList.size()][];
            for (int i = 0; i < result.length; i++) {
                result[i] = tempList.get(i);
            }
            return result;
        } else if (photos.size() == 0) {
            List<String[]> tempList = new ArrayList<String[]>();
            for (int row = 0; row < tempTable.length; row++) {
                for (int j=0;j<names.size();j++) {
                    String cell = tempTable[row][names.get(j)];
                    String webCell = null;
                    if (names.size()==webs.size()) {
                        webCell = tempTable[row][webs.get(j)];
                    } else if (names.size()*2==webs.size()) {
                        webCell = tempTable[row][webs.get(j*2)];
                    }
                    if (cell != null) {
                        String[] pair = new String[2];
                        pair[0] = cell;
                        pair[1] = webCell;
                        if (!pair[0].equals("")) {
                            tempList.add(pair);
                        }
                    }
                }
            }
            String[][] result = new String[tempList.size()][];
            for (int i = 0; i < result.length; i++) {
                result[i] = tempList.get(i);
            }
            return result;
        } else {
            return null;
        }
    }

}