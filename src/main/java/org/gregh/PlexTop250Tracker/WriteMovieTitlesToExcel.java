package org.gregh.PlexTop250Tracker;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Writes the missing movie names to an excel file
 * @author Greg Heiman
 */
public class WriteMovieTitlesToExcel {
    private XSSFWorkbook workbook;
    private OutputStream fileOut;
    private String fileOutName;

    public WriteMovieTitlesToExcel() {
        workbook = new XSSFWorkbook();
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    public OutputStream getFileOut() {
       return fileOut;
    }

    public void setFileOut(OutputStream fileOut) {
        this.fileOut = fileOut;
    }

    public String getFileOutName() {
        return fileOutName;
    }

    public void setFileOutName(String fileOutName) {
        this.fileOutName = fileOutName;
    }

    /**
     * Set the style for the movie name cells
     * @param workbook - the workbook that is currently being used
     * @return - the newly set up cell style
     */
    private CellStyle setSpreadsheetCellStyle(XSSFWorkbook workbook) {
        // Create a font for the rest of the spreadsheet
        Font spreadsheetFont = workbook.createFont();
        spreadsheetFont.setFontHeightInPoints((short) 16);

        // Use the font we just created to add to a cell style that will be applied to the movie titles
        CellStyle spreadsheetCellStyle = workbook.createCellStyle();
        spreadsheetCellStyle.setFont(spreadsheetFont);

        return spreadsheetCellStyle;
    }

    /**
     * Set the style for the hyperlink cells
     * @param workbook - the workbook that is currently being used
     * @return - the newly set up cell style
     */
    private CellStyle setHyperlinkStyle(XSSFWorkbook workbook) {
        // Create a font for the hyperlinks so that they look like hyperlinks
        Font hyperlinkFont = workbook.createFont();
        hyperlinkFont.setUnderline(Font.U_SINGLE);
        hyperlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hyperlinkFont.setFontHeightInPoints((short) 16);

        CellStyle hyperlinkStyle = workbook.createCellStyle();
        hyperlinkStyle.setFont(hyperlinkFont);

        return hyperlinkStyle;
    }

    /**
     *  Set the header row with the needed information and style
     * @param workbook - the current workbook that is being used
     * @param sheet - the current sheet that is being used
     */
    private static void setHeaderRow(XSSFWorkbook workbook, Sheet sheet) {
        // Set the font for the header row
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 24);
        headerFont.setColor(IndexedColors.RED.getIndex());

        // Create a cell style based on the font we just created
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Set the value of the header cell with the cell style we just created
        Row headerRow = sheet.createRow(0);
        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellValue("Name");
        headerCell.setCellStyle(headerCellStyle);

        // Create a cell to house the links in
        Cell linkCell = headerRow.createCell(1);
        linkCell.setCellValue("Links");
        linkCell.setCellStyle(headerCellStyle);

        // Create another cell in the next column that houses when the sheet was created
        Cell dateCell = headerRow.createCell(2);
        dateCell.setCellValue("This spreadsheet was created on: " + LocalDateTime.now());
        dateCell.setCellStyle(headerCellStyle);
    }

    /**
     * Write the movie names from the missing movie array list to the spreadsheet
     * @param missingMovieNames - The arraylist containing the names of the missing movies
     */
    public void writeMissingMoviesToSpreadsheet(ArrayList<String> missingMovieNames) {
        // Needed in order to allow for the easy creation of hyperlinks
        CreationHelper createHelper = getWorkbook().getCreationHelper();

        // If user wants MCPL URLs in the spreadsheet
        boolean userWantsLibraryURLs = decideWhetherToPutLibraryURLsInExcelSheet();

        // Set the name of the spreadsheet
        try {
            // Default LocalDateTime.now() has a colon in it causing problems on Windows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");

            setFileOutName(LocalDateTime.now().format(formatter) + "-MissingMovies.xlsx");
            setFileOut(new FileOutputStream(new File("./" + getFileOutName())));
            //setFileOut(new FileOutputStream("./" + getFileOutName()));
        } catch (FileNotFoundException e) {
            System.out.println("There was an issue finding or creating the needed spreadsheet file.");
            e.printStackTrace();
        }

        // Create a spreadsheet inside of workbook and set the header row information
        Sheet spreadsheet = getWorkbook().createSheet("Missing Movies");
        setHeaderRow(getWorkbook(), spreadsheet);

        // Go through the entire missingMovieNames list and add the names to the first column
        for (int i = 0; i < missingMovieNames.size(); i++) {
            // Create a new row for each movie
            // Needs to be + 1 in order to allow for the date at the top
            Row row = spreadsheet.createRow(i + 1);

            // For each row create a cell in the A column
            Cell movieCell = row.createCell(0);
            // Set the value of that A column to be the name of the movie that is missing
            movieCell.setCellValue(missingMovieNames.get(i));
            // Set the cell style for the movie titles
            movieCell.setCellStyle(setSpreadsheetCellStyle(getWorkbook()));

            // If user wants MCPL URLs in the spreadsheet
            if (userWantsLibraryURLs) {
                GetLibraryURLs libraryURLs = new GetLibraryURLs(missingMovieNames.get(i));
                // Set the library links in the B column
                Cell linkCell = row.createCell(1);
                // Set the value of the cell to the raw link
                linkCell.setCellValue(libraryURLs.createLibraryURL());
                // Create hyperlinks inside of the cells
                Hyperlink link = createHelper.createHyperlink(HyperlinkType.URL);
                link.setAddress(libraryURLs.createLibraryURL());
                linkCell.setHyperlink(link);
                // Set the style for the hyperlink cells
                linkCell.setCellStyle(setHyperlinkStyle(getWorkbook()));
            }
        }

        // Autosize the name column after all the movies have been added
        spreadsheet.autoSizeColumn(0);
        spreadsheet.autoSizeColumn(1);
        spreadsheet.autoSizeColumn(2);

        try {
            // Write the FileOutputStream to the workbook
            getWorkbook().write(getFileOut());
        } catch (IOException e) {
            System.out.println("There was a problem writing to the workbook.");
            e.printStackTrace();
        }

        try {
            // Close FileOutputStream and workbook when finished
            getFileOut().close();
            getWorkbook().close();
        } catch (IOException e) {
            System.out.println("There was a problem closing the workbook or the FileOutputStream");
            e.printStackTrace();
        }
    }

    private boolean decideWhetherToPutLibraryURLsInExcelSheet() {
       Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("Would you like to put MCPL URLs inside of the spreadsheet?");
            System.out.println("1. Yes");
            System.out.println("2. No");
            int answer = input.nextInt();

            switch (answer) {
                case 1:
                    System.out.println("\nThe spreadsheet will print library URLs.");
                    return true;
                case 2:
                    System.out.println("\nThe spreadsheet will not print library URLs");
                    return false;
                default:
                    System.out.println("Please enter in a valid option");
            }
        }

    }
}
