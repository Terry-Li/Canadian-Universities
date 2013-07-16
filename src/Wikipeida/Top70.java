/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Wikipeida;

import java.io.File;
import java.io.IOException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author lyf
 */
public class Top70 {
    public static void main(String[] args) throws BiffException, IOException {
        Workbook book = Workbook.getWorkbook(new File("top70.xls"));
        Sheet sheet = book.getSheet(0);
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<sheet.getRows();i++) {
            String name = sheet.getCell(1, i).getContents();
            String url = sheet.getCell(3, i).getContents();
            sb.append(name+"=="+url+"\n");
        }
        FileUtils.writeStringToFile(new File("../Input/top70.txt"), sb.toString());
    }
}
