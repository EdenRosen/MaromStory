package db;
import java.util.*;

/**
 * Manages named Excel tables through one shared database object
 */
public class ExcelDB {

    private static ExcelDB excelDB = null;
    private HashMap<String , ExcelTable> tables;


    private ExcelDB(){
        tables = new HashMap<String , ExcelTable>();
    }


    public static ExcelDB getInstance(){
        if (excelDB == null)
            excelDB = new ExcelDB();
        return excelDB;
    }


    public ExcelTable getTable(String tableName){
        return tables.get(tableName);
    }



    public ExcelTable createTableFromExcel(String tableName) {
        ExcelTable excelTable = new ExcelTable(tableName);
        tables.put(tableName, excelTable);
        return excelTable;
    }


    public void commit(){
        for (ExcelTable excelTable : tables.values()) {
            excelTable.WriteToFile();
        }
    }



    public ExcelTable createNewTable(String tableName, String[] headings) throws Exception {
        ExcelTable excelTable = new ExcelTable(tableName, headings);
        tables.put(tableName, excelTable);
        return excelTable;
    }

}
