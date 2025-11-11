import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class ExelSer {

    public static Set<Workbook> importExel (String name) {
        Workbook result = null;
        try (FileInputStream fis = new FileInputStream(name);
             Workbook workbook = new XSSFWorkbook(fis)) {
            result = workbook;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Collections.singleton(result);
    }

    public void printData (Workbook data ){
        Sheet sheet = data.getSheetAt(0);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    System.out.print(cell.toString() + "\t");
                }
                System.out.println();
            }
    }


    public static void main(String[] args) {
        /*Workbook data = null;
        String nameFile = "test-data.xlsx";
        data = (Workbook) ExelSer.importExel(nameFile);
*/


    }



}
