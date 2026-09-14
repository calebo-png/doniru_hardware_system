package com.example.projecthadwareinventorysystem;

import android.Manifest; // For permissions
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment; // For public storage (requires more handling)
import android.provider.MediaStore; // For MediaStore API
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // For permission checks
import androidx.core.content.FileProvider; // For sharing files
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class Reports extends AppCompatActivity {

    private static final String TAG = "Reports";

    private TextView textViewTotalDevices, textViewAvailableCount, textViewMaintenanceCount, textViewCheckedOutCount;
    private Spinner spinnerFilterStatus;
    private Button buttonToggleSort, buttonExportPdf; // Added buttonExportPdf
    private RecyclerView recyclerViewReportItems;

    private MyDBHelper dbHelper;
    private ReportItemsAdapter reportItemsAdapter;
    private List<InventoryItem> allInventoryItems;
    private List<InventoryItem> currentlyDisplayedItems;

    private boolean sortAscending = true;

    // For PDF generation and permissions
    private static final int REQUEST_CODE_WRITE_STORAGE_PERMISSION = 1001;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> createFileLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        dbHelper = new MyDBHelper(this);

        textViewTotalDevices = findViewById(R.id.textViewTotalDevices);
        textViewAvailableCount = findViewById(R.id.textViewAvailableCount);
        textViewMaintenanceCount = findViewById(R.id.textViewMaintenanceCount);
        textViewCheckedOutCount = findViewById(R.id.textViewCheckedOutCount);
        spinnerFilterStatus = findViewById(R.id.spinnerFilterStatus);
        buttonToggleSort = findViewById(R.id.buttonToggleSort);
        recyclerViewReportItems = findViewById(R.id.recyclerViewReportItems);
        buttonExportPdf = findViewById(R.id.buttonExportPdf); // Initialize PDF button

        allInventoryItems = new ArrayList<>();
        currentlyDisplayedItems = new ArrayList<>();

        recyclerViewReportItems.setLayoutManager(new LinearLayoutManager(this));
        reportItemsAdapter = new ReportItemsAdapter(this, currentlyDisplayedItems);
        recyclerViewReportItems.setAdapter(reportItemsAdapter);

        loadInventoryData();
        setupStatusFilterSpinner();
        setupSortButton();
        setupExportPdfButton(); // Setup for the PDF button

        filterAndSortData();

        // Initialize permission launcher for requesting WRITE_EXTERNAL_STORAGE permission.
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d(TAG, "WRITE_EXTERNAL_STORAGE permission granted.");
                promptToCreatePdfFile();
            } else {
                Log.w(TAG, "WRITE_EXTERNAL_STORAGE permission denied.");
                Toast.makeText(this, "Storage permission is required to save PDF.", Toast.LENGTH_LONG).show();
            }
        });

        // that allows the user to choose a location and name for a new file (the PDF report).
        createFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                Uri uri = result.getData().getData();
                Log.d(TAG, "User selected file location: " + uri.getPath());
                generateAndSavePdf(uri);
            } else {
                Log.w(TAG, "User cancelled file creation or an error occurred.");
                Toast.makeText(this, "PDF generation cancelled.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupExportPdfButton() {
        buttonExportPdf.setOnClickListener(v -> {
            Log.d(TAG, "Export PDF button clicked.");
            checkStoragePermissionAndCreatePdf();
        });
    }

    private void checkStoragePermissionAndCreatePdf() {
        // For Android 10 (API 29) and above, MediaStore or SAF is preferred for public directories.

        promptToCreatePdfFile();
    }

    private void promptToCreatePdfFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "InventoryReport_" + timeStamp + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        try {
            createFileLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Could not launch ACTION_CREATE_DOCUMENT intent", e);
            Toast.makeText(this, "Could not initiate PDF saving. Please check your file manager.", Toast.LENGTH_LONG).show();
        }
    }


    private void generateAndSavePdf(Uri uri) {
        if (currentlyDisplayedItems == null || currentlyDisplayedItems.isEmpty()) {
            Toast.makeText(this, "No data to export to PDF.", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        // Page properties (A4 size in points: 595 x 842)
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40; // Reduced margin for more content space
        int contentWidth = pageWidth - 2 * margin;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();
        Paint headerPaint = new Paint();
        Paint cellPaint = new Paint();

        // Title
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(20f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Inventory Report", pageWidth / 2f, margin, titlePaint);

        // Date
        paint.setTextSize(10f);
        paint.setColor(Color.DKGRAY);
        paint.setTextAlign(Paint.Align.RIGHT);
        String reportDate = "Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        canvas.drawText(reportDate, pageWidth - margin, margin, paint);
        paint.setTextAlign(Paint.Align.LEFT); // Reset alignment


        // Table Headers
        headerPaint.setColor(Color.BLACK);
        headerPaint.setTextSize(10f); // Smaller font for headers
        headerPaint.setFakeBoldText(true);
        cellPaint.setColor(Color.BLACK);
        cellPaint.setTextSize(9f); // Smaller font for cell data

        float yPosition = margin + 40; // Start below title and date
        float rowHeight = 20; // Height for each row
        float padding = 5;

        // Column widths (approximate, adjust as needed)
        float nameColWidth = contentWidth * 0.35f;
        float modelColWidth = contentWidth * 0.25f;
        float serialColWidth = contentWidth * 0.25f;
        float statusColWidth = contentWidth * 0.15f;

        // Draw Headers
        canvas.drawText("Name", margin + padding, yPosition, headerPaint);
        canvas.drawText("Model", margin + nameColWidth + padding, yPosition, headerPaint);
        canvas.drawText("Serial No.", margin + nameColWidth + modelColWidth + padding, yPosition, headerPaint);
        canvas.drawText("Status", margin + nameColWidth + modelColWidth + serialColWidth + padding, yPosition, headerPaint);
        yPosition += rowHeight;
        canvas.drawLine(margin, yPosition - (rowHeight / 2) + 2, pageWidth - margin, yPosition - (rowHeight / 2) + 2, headerPaint); // Underline headers


        // Draw Table Data
        for (InventoryItem item : currentlyDisplayedItems) {
            if (yPosition + rowHeight > pageHeight - margin) { // Check if new page is needed
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                yPosition = margin; // Reset Y for new page (you might want to redraw headers here too)
                canvas.drawText("Name", margin + padding, yPosition, headerPaint);
                canvas.drawText("Model", margin + nameColWidth + padding, yPosition, headerPaint);
                canvas.drawText("Serial No.", margin + nameColWidth + modelColWidth + padding, yPosition, headerPaint);
                canvas.drawText("Status", margin + nameColWidth + modelColWidth + serialColWidth + padding, yPosition, headerPaint);
                yPosition += rowHeight;
                canvas.drawLine(margin, yPosition - (rowHeight / 2) + 2, pageWidth - margin, yPosition - (rowHeight / 2) + 2, headerPaint);
            }

            canvas.drawText(truncateText(item.getName(), cellPaint, nameColWidth - (2 * padding)), margin + padding, yPosition, cellPaint);
            canvas.drawText(truncateText(item.getModel(), cellPaint, modelColWidth - (2 * padding)), margin + nameColWidth + padding, yPosition, cellPaint);
            canvas.drawText(truncateText(item.getSerial(), cellPaint, serialColWidth - (2 * padding)), margin + nameColWidth + modelColWidth + padding, yPosition, cellPaint);
            canvas.drawText(truncateText(item.getStatus(), cellPaint, statusColWidth - (2 * padding)), margin + nameColWidth + modelColWidth + serialColWidth + padding, yPosition, cellPaint);
            yPosition += rowHeight;
            canvas.drawLine(margin, yPosition - (rowHeight/2) +2, pageWidth - margin, yPosition - (rowHeight/2) + 2, paint); // Row separator
        }

        pdfDocument.finishPage(page);

        try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
            if (outputStream != null) {
                pdfDocument.writeTo(outputStream);
                Toast.makeText(this, "PDF Report saved to " + getFileNameFromUri(uri), Toast.LENGTH_LONG).show();
                Log.d(TAG, "PDF saved successfully to: " + uri.getPath());

                // Offer to open the PDF
                offerToOpenPdf(uri);

            } else {
                throw new IOException("OutputStream was null for URI: " + uri);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing PDF document", e);
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }

    // Helper to shorten text if it's too long for the column
    private String truncateText(String text, Paint paint, float maxWidth) {
        if (text == null) return "";
        float textWidth = paint.measureText(text);
        if (textWidth <= maxWidth) {
            return text;
        }
        int breakPoint = paint.breakText(text, true, maxWidth, null);
        return text.substring(0, breakPoint) + "...";
    }


    private String getFileNameFromUri(Uri uri) { // Retrieves the display name of a file from its content URI.
        String fileName = "InventoryReport.pdf";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file name from URI", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileName;
    }

    private void offerToOpenPdf(Uri pdfUri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(pdfUri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Important for content URIs
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY); // So it doesn't stay in back stack

            // Verify that an app exists to receive the intent
            PackageManager pm = getPackageManager();
            if (intent.resolveActivity(pm) != null) {
                startActivity(Intent.createChooser(intent, "Open PDF with..."));
            } else {
                Toast.makeText(this, "App found to open PDF.", Toast.LENGTH_LONG).show();
                Log.w(TAG, "No app can handle ACTION_VIEW for PDF.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error trying to open PDF", e);
            Toast.makeText(this, "Could not open PDF. Please find it in your Files app.", Toast.LENGTH_LONG).show();
        }
    }


    // Loads all inventory items from the database.
    private void loadInventoryData() {
        Log.d(TAG, "Loading inventory data from database...");
        allInventoryItems = dbHelper.getAllInventoryItems();
        if (allInventoryItems == null) {
            allInventoryItems = new ArrayList<>();
            Log.w(TAG, "No items found in the database or an error occurred.");
        } else {
            Log.d(TAG, "Loaded " + allInventoryItems.size() + " items from the database.");
        }
    }

  //Updates the summary counts displayed at the top of the reports screen.
    private void updateSummaryCounts(List<InventoryItem> itemsToSummarize) {
        // Check if the list of items to summarize is null.
        if (itemsToSummarize == null) {
            Log.w(TAG, "itemsToSummarize is null. Cannot update summary.");
            textViewTotalDevices.setText("0");
            textViewAvailableCount.setText("0");
            textViewMaintenanceCount.setText("0");
            textViewCheckedOutCount.setText("0");
            return; // Exit the method as there's nothing to process.
        }

        // Get the total number of items from the size of the list.
        long total = itemsToSummarize.size();
        // Initialize counters for each status.
        long available = 0;
        long maintenance = 0;
        long checkedOut = 0;

        // Iterate through each item in the list.
        for (InventoryItem item : itemsToSummarize) {
            if (item.getStatus() == null) continue;

            String status = item.getStatus().toLowerCase(Locale.getDefault());

            // Use a switch statement to increment the appropriate counter based on the item's status.
            switch (status) {
                case "available": available++; break;
                case "maintenance": maintenance++; break;
                case "checked out": checkedOut++; break;
            }
        }
        // Update the TextViews with the calculated counts.
        textViewTotalDevices.setText(String.valueOf(total));
        textViewAvailableCount.setText(String.valueOf(available));
        textViewMaintenanceCount.setText(String.valueOf(maintenance));
        textViewCheckedOutCount.setText(String.valueOf(checkedOut));

        Log.d(TAG, "Summary updated: Total=" + total + ", Avail=" + available + ", Maint=" + maintenance + ", CO=" + checkedOut);
    }

    private void setupStatusFilterSpinner() {
        String[] filterOptions = {"All Statuses", "Available", "Maintenance", "Checked Out"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilterStatus.setAdapter(adapter);
        spinnerFilterStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Filter selected: " + parent.getItemAtPosition(position).toString());
                filterAndSortData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    // Sets up the sort button to toggle between ascending and descending order and re-filters/sorts data.
    private void setupSortButton() {
        updateSortButtonText();
        buttonToggleSort.setOnClickListener(v -> {
            sortAscending = !sortAscending;
            Log.d(TAG, "Sort order toggled. Ascending: " + sortAscending);
            updateSortButtonText();
            filterAndSortData();
        });
    }

    private void updateSortButtonText() {
        buttonToggleSort.setText(sortAscending ? "Sort by Name (Asc)" : "Sort by Name (Desc)");
    }

    // Filters and sorts the inventory data based on selected criteria and updates the displayed list.
    private void filterAndSortData() {
        if (allInventoryItems == null) {
            Log.w(TAG, "allInventoryItems is null. Cannot filter/sort.");
            currentlyDisplayedItems.clear();
            if (reportItemsAdapter != null) reportItemsAdapter.updateData(currentlyDisplayedItems);
            updateSummaryCounts(currentlyDisplayedItems);
            return;
        }
        List<InventoryItem> filteredList = new ArrayList<>(allInventoryItems);
        String selectedFilter = spinnerFilterStatus.getSelectedItem().toString();
        if (!selectedFilter.equals("All Statuses")) {
            filteredList = filteredList.stream()
                    .filter(item -> item.getStatus() != null && item.getStatus().equalsIgnoreCase(selectedFilter))
                    .collect(Collectors.toList());
        }
        Log.d(TAG, "After filtering by '" + selectedFilter + "': " + filteredList.size() + " items.");
        Collections.sort(filteredList, (o1, o2) -> {
            int comparison = o1.getName().compareToIgnoreCase(o2.getName());
            return sortAscending ? comparison : -comparison;
        });
        Log.d(TAG, "After sorting (Ascending=" + sortAscending + "): First item (if any): " + (filteredList.isEmpty() ? "N/A" : filteredList.get(0).getName()));
        currentlyDisplayedItems.clear();
        currentlyDisplayedItems.addAll(filteredList);
        if (reportItemsAdapter != null) reportItemsAdapter.updateData(currentlyDisplayedItems);
        updateSummaryCounts(currentlyDisplayedItems); // Update summary based on the filtered and sorted list
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
