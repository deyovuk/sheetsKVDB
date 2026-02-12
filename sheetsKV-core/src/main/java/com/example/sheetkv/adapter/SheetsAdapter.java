package com.example.sheetkv.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.sheetkv.config.SheetProperties;
import com.example.sheetkv.exception.BackendException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DeleteSheetRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

@Component
public class SheetsAdapter {
    private static final Logger logger = LoggerFactory.getLogger(SheetsAdapter.class);

    private final Sheets sheets;
    private final SheetProperties properties;

    public SheetsAdapter(Sheets sheets, SheetProperties properties) {
        this.sheets = sheets;
        this.properties = properties;
    }

    public Spreadsheet getSpreadsheetMetadata() {
        try {
            return sheets.spreadsheets().get(properties.getSpreadsheetId())
                    .setFields("sheets.properties")
                    .execute();
        } catch (IOException ex) {
            logger.error("sheets.metadata failed", ex);
            throw new BackendException("Failed to fetch spreadsheet metadata", ex);
        }
    }

    public List<Sheet> listSheets() {
        Spreadsheet metadata = getSpreadsheetMetadata();
        return Optional.ofNullable(metadata.getSheets()).orElse(List.of());
    }

    public List<String> readColumnA(String sheetName) {
        try {
            ValueRange range = sheets.spreadsheets().values()
                    .get(properties.getSpreadsheetId(), sheetName + "!A:A")
                    .execute();
            List<List<Object>> values = Optional.ofNullable(range.getValues()).orElse(List.of());
            List<String> ids = new ArrayList<>();
            for (List<Object> row : values) {
                if (row.isEmpty()) {
                    ids.add("");
                } else {
                    Object value = row.get(0);
                    ids.add(value == null ? "" : String.valueOf(value));
                }
            }
            return ids;
        } catch (IOException ex) {
            logger.error("sheets.readColumnA failed sheet={}", sheetName, ex);
            throw new BackendException("Failed to read column A", ex);
        }
    }

    public String readCell(String sheetName, int row) {
        try {
            ValueRange range = sheets.spreadsheets().values()
                    .get(properties.getSpreadsheetId(), sheetName + "!B" + row)
                    .execute();
            List<List<Object>> values = range.getValues();
            if (values == null || values.isEmpty() || values.get(0).isEmpty()) {
                return null;
            }
            Object value = values.get(0).get(0);
            return value == null ? null : String.valueOf(value);
        } catch (IOException ex) {
            logger.error("sheets.readCell failed sheet={} row={}", sheetName, row, ex);
            throw new BackendException("Failed to read cell", ex);
        }
    }

    public void updateCell(String sheetName, int row, String value) {
        try {
            ValueRange body = new ValueRange().setValues(List.of(List.of(value)));
            sheets.spreadsheets().values()
                    .update(properties.getSpreadsheetId(), sheetName + "!B" + row, body)
                    .setValueInputOption("RAW")
                    .execute();
        } catch (IOException ex) {
            logger.error("sheets.updateCell failed sheet={} row={}", sheetName, row, ex);
            throw new BackendException("Failed to update cell", ex);
        }
    }

    public int appendRow(String sheetName, String id, String value) {
        try {
            int row = readColumnA(sheetName).size() + 1;
            ValueRange body = new ValueRange().setValues(List.of(List.of(id, value)));
            sheets.spreadsheets().values()
                    .update(properties.getSpreadsheetId(), sheetName + "!A" + row + ":B" + row, body)
                    .setValueInputOption("RAW")
                    .execute();
            return row;
        } catch (IOException ex) {
            logger.error("sheets.appendRow failed sheet={}", sheetName, ex);
            throw new BackendException("Failed to append row", ex);
        }
    }

    public void deleteRow(String sheetName, int row) {
        try {
            int sheetId = getSheetId(sheetName);
            DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
                    .setRange(new DimensionRange()
                            .setSheetId(sheetId)
                            .setDimension("ROWS")
                            .setStartIndex(row - 1)
                            .setEndIndex(row));
            BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
                    .setRequests(List.of(new Request().setDeleteDimension(deleteRequest)));
            sheets.spreadsheets().batchUpdate(properties.getSpreadsheetId(), batch).execute();
        } catch (IOException ex) {
            logger.error("sheets.deleteRow failed sheet={} row={}", sheetName, row, ex);
            throw new BackendException("Failed to delete row", ex);
        }
    }

    public void createSheet(String name) {
        try {
            AddSheetRequest addRequest = new AddSheetRequest()
                    .setProperties(new com.google.api.services.sheets.v4.model.SheetProperties().setTitle(name));
            BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
                    .setRequests(List.of(new Request().setAddSheet(addRequest)));
            sheets.spreadsheets().batchUpdate(properties.getSpreadsheetId(), batch).execute();
        } catch (IOException ex) {
            logger.error("sheets.createSheet failed name={}", name, ex);
            throw new BackendException("Failed to create sheet", ex);
        }
    }

    public void deleteSheet(String name) {
        try {
            int sheetId = getSheetId(name);
            DeleteSheetRequest deleteRequest = new DeleteSheetRequest().setSheetId(sheetId);
            BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
                    .setRequests(List.of(new Request().setDeleteSheet(deleteRequest)));
            sheets.spreadsheets().batchUpdate(properties.getSpreadsheetId(), batch).execute();
        } catch (IOException ex) {
            logger.error("sheets.deleteSheet failed name={}", name, ex);
            throw new BackendException("Failed to delete sheet", ex);
        }
    }

    public void renameSheet(String oldName, String newName) {
        try {
            int sheetId = getSheetId(oldName);
            com.google.api.services.sheets.v4.model.SheetProperties props = new com.google.api.services.sheets.v4.model.SheetProperties()
                    .setSheetId(sheetId).setTitle(newName);
            UpdateSheetPropertiesRequest updateRequest = new UpdateSheetPropertiesRequest()
                    .setProperties(props)
                    .setFields("title");
            BatchUpdateSpreadsheetRequest batch = new BatchUpdateSpreadsheetRequest()
                    .setRequests(List.of(new Request().setUpdateSheetProperties(updateRequest)));
            sheets.spreadsheets().batchUpdate(properties.getSpreadsheetId(), batch).execute();
        } catch (IOException ex) {
            logger.error("sheets.renameSheet failed old={} new={}", oldName, newName, ex);
            throw new BackendException("Failed to rename sheet", ex);
        }
    }

    private int getSheetId(String name) {
        return listSheets().stream()
                .map(Sheet::getProperties)
                .filter(props -> props != null && name.equals(props.getTitle()))
                .map(com.google.api.services.sheets.v4.model.SheetProperties::getSheetId)
                .findFirst()
                .orElseThrow(() -> new BackendException("Sheet not found: " + name));
    }

    private int parseRowFromRange(String range) {
        if (range == null) {
            return -1;
        }
        int bang = range.indexOf('!');
        if (bang >= 0) {
            range = range.substring(bang + 1);
        }
        String digits = range.replaceAll("[^0-9]", "");
        if (digits.isBlank()) {
            return -1;
        }
        return Integer.parseInt(digits);
    }
}
