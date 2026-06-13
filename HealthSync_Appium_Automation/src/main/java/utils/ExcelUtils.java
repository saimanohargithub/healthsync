package utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelUtils {
    private static String excelPath = "excel_reports/ExecutionReport.xlsx";
    private static Workbook workbook;
    private static Sheet sheet;
    private static Sheet summarySheet;
    private static Sheet defectSheet;

    public static void createExcelReport() {
        try {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Test Execution");
            summarySheet = workbook.createSheet("Summary");
            defectSheet = workbook.createSheet("Defect Analysis");

            // Create headers for Test Execution
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Test Case ID", "Module", "Test Scenario", "Expected Result", "Actual Result", "Status", "Execution Time", "Device Name", "Screenshot Path", "Remarks"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Create headers for Summary
            Row summaryHeader = summarySheet.createRow(0);
            String[] sHeaders = {"Total Test Cases", "Passed", "Failed", "Blocked", "Pending", "Pass Percentage", "Fail Percentage"};
            for (int i = 0; i < sHeaders.length; i++) {
                Cell cell = summaryHeader.createCell(i);
                cell.setCellValue(sHeaders[i]);
            }

            // Create headers for Defects
            Row defectHeader = defectSheet.createRow(0);
            String[] dHeaders = {"Defect ID", "Module", "Severity", "Priority", "Description", "Root Cause", "Status"};
            for (int i = 0; i < dHeaders.length; i++) {
                Cell cell = defectHeader.createCell(i);
                cell.setCellValue(dHeaders[i]);
            }

            FileOutputStream out = new FileOutputStream(new File(excelPath));
            workbook.write(out);
            out.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void updateTestResult(String testId, String module, String scenario, String status, String execTime, String deviceName, String screenshotPath, String remarks) {
        try {
            FileInputStream in = new FileInputStream(new File(excelPath));
            workbook = new XSSFWorkbook(in);
            sheet = workbook.getSheet("Test Execution");
            in.close();

            int rowCount = sheet.getLastRowNum();
            Row row = sheet.createRow(rowCount + 1);

            row.createCell(0).setCellValue(testId);
            row.createCell(1).setCellValue(module);
            row.createCell(2).setCellValue(scenario);
            row.createCell(3).setCellValue("Feature should work as expected");
            row.createCell(4).setCellValue(status.equals("PASS") ? "Feature works as expected" : "Feature failed or errored");
            row.createCell(5).setCellValue(status);
            row.createCell(6).setCellValue(execTime);
            row.createCell(7).setCellValue(deviceName);
            row.createCell(8).setCellValue(screenshotPath);
            row.createCell(9).setCellValue(remarks);

            FileOutputStream out = new FileOutputStream(new File(excelPath));
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
