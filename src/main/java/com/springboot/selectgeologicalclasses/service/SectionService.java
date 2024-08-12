package com.springboot.selectgeologicalclasses.service;

import com.springboot.selectgeologicalclasses.exception.ResourceNotFoundException;
import com.springboot.selectgeologicalclasses.model.JobStatus;
import com.springboot.selectgeologicalclasses.model.Section;
import com.springboot.selectgeologicalclasses.repositories.JobStatusRepository;
import com.springboot.selectgeologicalclasses.repositories.SectionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class SectionService {

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Autowired
    private SectionRepository sectionRepository;

    public ResponseEntity<byte[]> getExportedFile(Long id) throws IOException {
        JobStatus jobStatus = jobStatusRepository.findById(id).orElse(null);
        if (jobStatus == null || !"EXPORT".equals(jobStatus.getJobType())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        if ("IN_PROGRESS".equals(jobStatus.getStatus())) {
            throw new IllegalStateException("Export is still in progress");
        }
        if ("DONE".equals(jobStatus.getStatus())) {
            String userHome = System.getProperty("user.home");
            String documentsPath = userHome + File.separator + "Documents" + File.separator + "sections.xlsx";
            File file = new File(documentsPath);
            Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
            Sheet sheet = workbook.getSheetAt(0);
            StringBuilder csvContent = new StringBuilder();

            for (Row row : sheet) {
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            csvContent.append(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            csvContent.append(cell.getNumericCellValue());
                            break;
                        default:
                            csvContent.append("");
                    }
                    csvContent.append(",");
                }
                csvContent.setLength(csvContent.length() - 1); // Remove trailing comma
                csvContent.append("\n");
            }
            workbook.close();
            byte[] csvBytes = csvContent.toString().getBytes();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/csv")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=sections.csv")
                    .body(csvBytes);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    public ResponseEntity<String> updateSection(Long id, Section sectionDetails) {
        Section section = sectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Section not exist with id :" + id));

        section.setName(sectionDetails.getName());
        // Update geologicalClasses
        section.getGeologicalClasses().clear();
        section.getGeologicalClasses().addAll(sectionDetails.getGeologicalClasses());

        sectionRepository.save(section);
        return ResponseEntity.ok("Section with ID " + id + " updated successfully.");
    }
}