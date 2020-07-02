package com.example.uhf.fragment;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.os.Debug;
import android.provider.MediaStore.Files;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;

import jxl.Cell;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
/**
 * Created by Administrator on 2018/7/25.
 */

public class FileXls {

    private static final int DEFAULT_SHEET = 0;

    public static String readXLS(String path) {
        String str = "";

        try {
            Workbook workbook = Workbook.getWorkbook(new File(path));
            Sheet sheet = workbook.getSheet(0);
            int columnCount = sheet.getColumns();
            int rowCount = sheet.getRows();

            Cell cell = null;
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    cell = sheet.getCell(j, i);
                    String temp2 = "";
                    if (cell.getType() == CellType.NUMBER) {
                        temp2 = ((NumberCell) cell).getValue() + "";
                    } else if (cell.getType() == CellType.DATE) {
                        temp2 = "" + ((DateCell) cell).getDate();
                    } else {
                        temp2 = "" + cell.getContents();
                    }
                    str = str + "  " + temp2;
                }
                str += "\n";
            }
            workbook.close();
        } catch (Exception e) {
        }

        return str;
    }

    public static ArrayList<HashMap<String, Object>> readXLSmap(String path) {
        String str = "";
        ArrayList<HashMap<String, Object>> listmap = new ArrayList<HashMap<String, Object>>();
        try {
            Workbook workbook = Workbook.getWorkbook(new File(path));
            Sheet sheet = workbook.getSheet(0);
            int columnCount = sheet.getColumns();
            int rowCount = sheet.getRows();

            Cell cell = null;
            for (int i = 1; i < rowCount; i++) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("tagUii", sheet.getCell(0, i).getContents());
              //  map.put("jl", sheet.getCell(1, i).getContents());



                listmap.add(map);

            }
            workbook.close();
        } catch (Exception e) {
        }

        return listmap;
    }

    public static List<ArrayList<String>> readXLSX(String path) {
        String v = null;
        List<String> list = new ArrayList<String>();
        List<ArrayList<String>> table = new ArrayList<ArrayList<String>>();

        try {
            ZipFile file = new ZipFile(new File(path));

            ZipEntry sharedStringXML = file.getEntry("xl/sharedStrings.xml");
            InputStream inputStream = file.getInputStream(sharedStringXML);
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                switch (event) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();
                        if (tag.equalsIgnoreCase("t")) {
                            list.add(parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                    default:
                        break;
                }

                event = parser.next();
            }

            ZipEntry sheetXML = file.getEntry("xl/worksheets/sheet1.xml");
            InputStream inputStreamsheet = file.getInputStream(sheetXML);
            parser = Xml.newPullParser();
            parser.setInput(inputStreamsheet, "utf-8");
            event = parser.getEventType();

            ArrayList<String> row = null;
            boolean isText = false;
            while (event != XmlPullParser.END_DOCUMENT) {

                switch (event) {
                    case XmlPullParser.START_TAG:
                        String tag = parser.getName();

                        if (tag.equalsIgnoreCase("row")) {
                            row = new ArrayList<String>();
                            table.add(row);
                        } else if (tag.equalsIgnoreCase("c")) {
                            String t = parser.getAttributeValue(null, "t");

                            if (t != null) {
                                isText = true;
                            } else {
                                isText = false;
                            }
                        } else if (tag.equalsIgnoreCase("v")) {
                            String cell = parser.nextText();

                            if (cell != null) {
                                if (isText) {
                                    row.add(list.get(Integer.parseInt(cell)));
                                } else {
                                    row.add(cell);
                                }
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equalsIgnoreCase("row") && v != null) {
                        }
                        break;
                }

                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return table;
    }

    public static boolean writeXLS(String path, List<Object> table) {
        File file = createXLS(path);

        if (file == null) {
            return false;
        } else {

            try {
                return addData(file, table);
            } catch (RowsExceededException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;

            } catch (WriteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

    }

    private static File createXLS(String path) {

        File file = null;

        try {
            file = new File(path);
            if (file.exists())
                return file;
            WritableWorkbook book = Workbook.createWorkbook(file);
            WritableSheet sheet = book.createSheet("sheet1", 0);

            book.write();
            book.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return file;
        }
    }

    private static boolean addData(File file, List<Object> table)
            throws IOException, RowsExceededException, WriteException {

        Workbook wb = null;
        try {
            wb = Workbook.getWorkbook(file);
        } catch (BiffException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        WritableWorkbook book = Workbook.createWorkbook(file, wb);
        WritableSheet sheet = book.getSheet(0);
        // List<String> attrs = table;
        int i = sheet.getRows();
        for (int j = 0; j < table.size(); j++) {
            List<String> attrs = (List<String>) table.get(j);
            for (int attr = 0; attr < attrs.size(); attr++) {
                Label label = new Label(attr, i + j, attrs.get(attr));
                sheet.addCell(label);

            }
        }
		/*
		 * for (int attr = 0; attr < attrs.size(); attr++) { Label label = new
		 * Label(attr, i, attrs.get(attr)); sheet.addCell(label); }
		 */

        book.write();
        book.close();
        wb.close();
        return true;

    }

    public static final int CREATE_FAIL = -1;
    public static final int ADD_DATA_FAIL = -2;
}
