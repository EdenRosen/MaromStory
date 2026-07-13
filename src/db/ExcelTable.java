package db;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Reads writes queries and sorts a table backed by an Excel file
 */
public class ExcelTable {

    private HashMap<String, String[]> excelMap = new HashMap<String, String[]>();
    private String filename;
    private String[] headings;
    private final String EMPTY_FILE = "Game.db";



    public ExcelTable(String tableName){
        try {
            filename = "db_tables\\" + tableName+".xlsx";
            File file = new File(filename);
            FileInputStream fis = new FileInputStream(file);

            XSSFWorkbook wb = new XSSFWorkbook(fis);
            XSSFSheet sheet = wb.getSheetAt(0);

            boolean isFirstRow = true;
            boolean isFirstColumn;
            String key;
            String[] values;
            for (Row row : sheet) {
                Iterator<Cell> columnsItr = row.cellIterator();
                isFirstColumn = true;
                key = null;
                values = new String[100];
                int i = 0;


                while (columnsItr.hasNext()) {
                    Cell cell = columnsItr.next();


                    if (isFirstColumn) {
                        isFirstColumn = false;
                        switch (cell.getCellType()) {
                            case STRING:
                                key = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                key = NumberToTextConverter.toText(cell.getNumericCellValue());
                                break;
                            default:
                        }
                    }

                    switch (cell.getCellType()) {
                        case STRING:
                            values[i] = cell.getStringCellValue();
                            break;
                        case NUMERIC:
                            values[i] = NumberToTextConverter.toText(cell.getNumericCellValue());
                            break;
                        default:
                    }
                    i++;
                }
                if (isFirstRow){
                    String[] headings = new String[i];
                    System.arraycopy(values,0,headings,0,i);
                    this.headings = headings;
                    isFirstRow = false;
                }
                else
                    excelMap.put(key, values);
            }
        }

        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("No file found or file is not accessible");
        }
    }


    public ExcelTable(String tableName, String[] headings) throws Exception {
        filename = "db_tables\\" + tableName+".xlsx";
        File file = new File(filename);
        try {
            FileInputStream fis = new FileInputStream(file);
            throw new Exception("File is already exist!");
        } catch (FileNotFoundException e) {
            this.headings = headings;
            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet(EMPTY_FILE);

            Row row = sheet.createRow(0);
            int columnCount = 0;
            for (String heading : headings) {
                Cell cell = row.createCell(columnCount++);
                cell.setCellValue(heading);
            }

            try (FileOutputStream outputStream = new FileOutputStream(filename)) {
                workbook.write(outputStream);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }




    public void insertRow(String[] row) throws Exception {
        String tempRow[] = row.clone();
        String key = row[0];
        if (excelMap.putIfAbsent(key, tempRow) != null)
            throw new Exception("Primary key already exist!");
    }



    public void updateRow(String[] row) throws Exception {
        String tempRow[] = row.clone();
        String key = row[0];
        if (excelMap.get(key) != null)
            excelMap.put(key, tempRow);
        else
            throw new Exception("Can't find primary key in table!");
    }


    public String[] getFields(String key){
       return excelMap.get(key);
    }




    public String[] getFields(String key, int[] index){
        String[] row = new String[index.length];
        for (int i = 0; i < index.length; i++) {
            row[i] = excelMap.get(key)[index[i]];
        }
        return row;
    }


    public void deleteRow(String key) {
        excelMap.remove(key);
    }


    public void deleteAllRows() {
        excelMap.clear();
    }



    private <K, V extends Comparable<? super String[]>> Comparator<Map.Entry<String, String[]>> compareStrByXValue(int index) {
        return (Comparator<Map.Entry<String, String[]>> & Serializable)
                (c1, c2) -> c1.getValue()[index].compareTo(c2.getValue()[index]);
    }

    private <K, V extends Comparable<? super String[]>> Comparator<Map.Entry<String, String[]>> compareIntByXValue(int index) {
        return (Comparator<Map.Entry<String, String[]>> & Serializable)
                (c1, c2) -> (Integer.valueOf(c1.getValue()[index])).compareTo( Integer.valueOf(c2.getValue()[index]));
    }



    private boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }





    public void sortTable(int index){
        sortTable(index, false);
    }

    public void sortTable(int index, boolean isDescending)
    {

        List<Map.Entry<String, String[]> > list = new LinkedList<Map.Entry<String, String[]> >(excelMap.entrySet());


        if (isNumeric(list.get(0).getValue()[index])) {
            list.sort(compareIntByXValue(index));
        }
        else {
            list.sort(compareStrByXValue(index));
        }

        if (isDescending) {
            Collections.reverse(list);
        }


        HashMap<String, String[]> tempMap = new LinkedHashMap<String, String[]>();
        for (Map.Entry<String, String[]> mapEntry : list) {
            tempMap.put(mapEntry.getKey(), mapEntry.getValue());
        }
        this.excelMap = tempMap;
    }


    public void sortByKey() {
        sortTable(0, false);
    }



    public String[][] getTableAsMatrix(){
        String[][] table = new String[excelMap.size()][headings.length];
        int rowCount = 0;
        for (String key : excelMap.keySet()) {
            table[rowCount] = getFields(key);
            rowCount++;
        }
        return table;
    }


    public String[][] getTableAsMatrix(int[] index){
        String[][] table = new String[excelMap.size()][index.length];
        int rowCount = 0;
        for (String key : excelMap.keySet()) {
            table[rowCount] = getFields(key, index);
            rowCount++;
        }
        return table;
    }

    public void WriteToFile() {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(EMPTY_FILE);

        Row firstRow = sheet.createRow(0);
        int firstRowColumn = 0;
        for (String heading : headings) {
            Cell keyCell = firstRow.createCell(firstRowColumn);
            keyCell.setCellValue(heading);
            firstRowColumn++;
        }
        int rowCount = 1;

        for (String key : excelMap.keySet()) {
            Row row = sheet.createRow(rowCount++);

            int columnCount = 0;
            for (String value : excelMap.get(key)) {
                Cell valueCell = row.createCell(columnCount++);
                valueCell.setCellValue(value);
            }

        }
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            workbook.write(outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showTable(String title, String[][] tableMatrix, int width, int height) {
        JFrame frame = new JFrame(title);
        JScrollPane scrollPane = new JScrollPane();

        JTable jTable = new JTable(tableMatrix, headings);

        scrollPane.setViewportView(jTable);
        jTable.setDefaultEditor(Object.class, null);
        frame.setSize(width, height);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.add(scrollPane);
    }
}
