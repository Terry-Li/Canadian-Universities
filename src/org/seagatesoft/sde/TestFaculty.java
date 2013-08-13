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
public class TestFaculty
{
    public static Formatter output;
    public static String namesFile = "Group/Names.txt";
    public static double similarityTreshold = 0.8; //default: 0.80
    public static boolean ignoreFormattingTags = false; //default: false
    public static boolean useContentSimilarity = false; //default: false
    public static int maxNodeInGeneralizedNodes = 1; //default: 9
    public static boolean humanConsumable = true;
    public static boolean intermediateResult = false;
    public static String url = "http://www.cee.duke.edu/faculty";
    
    static {
        try {
            String resultOutput = "26CSFaculty/test.html";
            output = new Formatter(new File(resultOutput));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TestFaculty.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        process();
    }
    
    
    public static void process() {
        System.out.println("Start processing: " + url);
        for (int nodes=1;nodes<=2;nodes++) {
            maxNodeInGeneralizedNodes = nodes;
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
            if (dataTables.size()!=0) {
                break;
            }
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
            if (!email && FacultyList.emailColumn1(tempTable, i)) {
                FacultyList.addEmail(result, tempTable, i);
                //System.out.println(tempTable[0][8]);
                email = true;
            }
            if (!phone && FacultyList.phoneColumn(tempTable, i)){
                FacultyList.addPhone(result, tempTable, i);
                phone = true;
            }
        }
        for (int i=0; i<cols; i++) {
            if (email) {
                break;
            }
            if (FacultyList.emailColumn2(tempTable, i)) {
                FacultyList.addEmail(result, tempTable, i);
                email = true;
            }
        }
        return result;
    }

}