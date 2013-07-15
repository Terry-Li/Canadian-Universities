package org.seagatesoft.sde;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class AppConsole
{
	/*
	 * Formatter untuk menulis ke file output
	 */
	public static Formatter output;
	public static String namesFile = "Names.txt";


	/**
	 * Method main untuk aplikasi utama yang berbasis konsol. Ada empat argumen yang bisa diberikan, 
	 * urutannya URI file input, URI file output, similarity treshold, jumlah node maksimum dalam generalized node.
	 *  
	 * @param args Parameter yang dimasukkan pengguna aplikasi konsol
	 */
	   public static void main(String args[]) {
        // parameter default
        String intputFile = "TestFaculty.txt";
        ArrayList<String> facultylists = DealFile.readFile(intputFile);
        //System.out.println(facultylists.size());
        for (int r = 9; r < 10; r++) {
            System.out.println("r:" + r);
            String input = facultylists.get(r);
            String resultOutput = "TestResult\\MDR" + r + ".html";
            double similarityTreshold = 0.6; //default: 0.80
            boolean ignoreFormattingTags = false; //default: false
            boolean useContentSimilarity = false; //default: false
            int maxNodeInGeneralizedNodes = 9; //default: 9
            boolean humanConsumable = false;

            // parameter dari pengguna, urutannya URI file input, URI file output, similarity treshold, jumlah node maksimum dalam generalized node
            // parameter yang wajib adalah parameter URI file input

            try {
                // siapkan file output
                output = new Formatter(resultOutput);
                // buat objek TagTreeBuilder yang berbasis parser DOM
                TagTreeBuilder builder = new DOMParserTagTreeBuilder();
                // bangun pohon tag dari file input menggunakan objek TagTreeBuilder yang telah dibuat
                TagTree tagTree = builder.buildTagTree(input, ignoreFormattingTags);
                //print(A.getRoot(), " ");
                //printHTML( A.getRoot());
                // buat objek TreeMatcher yang menggunakan algoritma simple tree matching
                TreeMatcher matcher = new SimpleTreeMatching();
                // buat objek DataRegionsFinder yang menggunakan algoritma mining data regions dan
                // menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
                DataRegionsFinder dataRegionsFinder = new MiningDataRegions(matcher);
                // cari data region pada pohon tag menggunakan objek DataRegionsFinder yang telah dibuat
                List<DataRegion> dataRegions = dataRegionsFinder.findDataRegions(tagTree.getRoot(), maxNodeInGeneralizedNodes, similarityTreshold);
                // buat objek DataRecordsFinder yang menggunakan metode mining data records dan
                // menggunakan algoritma pencocokan pohon yang telah dibuat sebelumnya
                DataRecordsFinder dataRecordsFinder = new MiningDataRecords(matcher);
                // buat matriks DataRecord untuk menyimpan data record yang teridentifikasi oleh 
                // DataRecordsFinder dari List<DataRegion> yang ditemukan
                DataRecord[][] dataRecords = new DataRecord[dataRegions.size()][];

                // identifikasi data records untuk tiap2 data region 
                for (int dataRecordArrayCounter = 0; dataRecordArrayCounter < dataRegions.size(); dataRecordArrayCounter++) {
                    DataRegion dataRegion = dataRegions.get(dataRecordArrayCounter);
                    dataRecords[ dataRecordArrayCounter] = dataRecordsFinder.findDataRecords(dataRegion, similarityTreshold);
                }

                // buat objek ColumnAligner yang menggunakan algoritma partial tree alignment
                ColumnAligner aligner = null;
                if (useContentSimilarity) {
                    aligner = new PartialTreeAligner(new EnhancedSimpleTreeMatching());
                } else {
                    aligner = new PartialTreeAligner(matcher);
                }
                List<String[][]> dataTables = new ArrayList<String[][]>();
                List<String[][]> tempTables = new ArrayList<String[][]>();

                // bagi tiap2 data records ke dalam kolom sehingga berbentuk tabel
                // dan buang tabel yang null
                for (int tableCounter = 0; tableCounter < dataRecords.length; tableCounter++) {
                    String[][] dataTable = aligner.alignDataRecords(dataRecords[tableCounter]);

                    if (dataTable != null && FacultyList.identify(dataTable)) {
                        truncate(dataTable);
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


                // write extracted data to output file
                output.format("<html><head><title>Extraction Result</title>");
                output.format("<style type=\"text/css\">table {border-collapse: collapse;} td {padding: 5px} table, th, td { border: 3px solid black;} </style>");
                output.format("</head><body>");
                int tableCounter = 1;
                //System.out.println("oh yeah");
                output.format("<a href=\"%s\">Faculty Page</a>\n\n", facultylists.get(r));
                for (String[][] table : dataTables) {
                    //	System.out.println("table.length:" + table.length);
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
                            //System.out.println(itemText);
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
                System.exit(1);
            } catch (FileNotFoundException exception) {
                exception.printStackTrace();
                System.exit(2);
            } catch (IOException exception) {
                exception.printStackTrace();
                System.exit(3);
            } catch (SAXException exception) {
                exception.printStackTrace();
                System.exit(4);
            } catch (Exception exception) {
                exception.printStackTrace();
                System.exit(5);
            } finally {
                if (output != null) {
                    output.close();
                }
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