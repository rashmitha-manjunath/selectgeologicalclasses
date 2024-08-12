package com.springboot.selectgeologicalclasses.service;

import com.springboot.selectgeologicalclasses.model.GeologicalClass;
import com.springboot.selectgeologicalclasses.model.Section;
import com.springboot.selectgeologicalclasses.model.JobStatus;
import com.springboot.selectgeologicalclasses.repositories.JobStatusRepository;
import com.springboot.selectgeologicalclasses.repositories.SectionRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Service
public class ExcelService {

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private JobStatusRepository jobStatusRepository;

    @Async
    public Future<Long> exportSectionsToExcelAsync() {
        JobStatus jobStatus = new JobStatus();
        jobStatus.setStatus("IN_PROGRESS");
        jobStatus.setJobType("EXPORT");
        jobStatus = jobStatusRepository.save(jobStatus);
        Long jobId = jobStatus.getId();

        try {
            exportSectionsToExcel();
            jobStatus.setStatus("DONE");
        } catch (IOException e) {
            jobStatus.setStatus("ERROR");
            throw new RuntimeException(e);
        } finally {
            jobStatusRepository.save(jobStatus);
        }

        return new AsyncResult<>(jobId);
    }

    @Async
    public Future<Long> importSectionsFromExcelAsync(MultipartFile file) throws IOException {
        com.springboot.selectgeologicalclasses.model.JobStatus jobStatus = new com.springboot.selectgeologicalclasses.model.JobStatus();
        jobStatus.setStatus("IN_PROGRESS");
        jobStatus.setJobType("IMPORT");
        jobStatus = jobStatusRepository.save(jobStatus);
        Long jobId = jobStatus.getId();

        try {
            importSectionsFromExcel(file);
            jobStatus.setStatus("DONE");
        } catch (IOException e) {
            jobStatus.setStatus("ERROR");
            throw new RuntimeException(e);
        } finally {
            jobStatusRepository.save(jobStatus);
        }

        return new AsyncResult<>(jobId);
    }

    public void exportSectionsToExcel() throws IOException {
        List<Section> sections = sectionRepository.findAll();
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sections");

        // Determine the maximum number of geological classes in any section
        int maxClasses = sections.stream()
                .mapToInt(section -> section.getGeologicalClasses().size())
                .max()
                .orElse(0);

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Section name");
        int cellNum = 1;
        for (int i = 1; i <= maxClasses; i++) {
            headerRow.createCell(cellNum++).setCellValue("class " + i + " name");
            headerRow.createCell(cellNum++).setCellValue("class " + i + " code");
        }

        // Populate rows with section data
        int rowNum = 1;
        for (Section section : sections) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(section.getName());

            cellNum = 1;
            for (GeologicalClass geoClass : section.getGeologicalClasses()) {
                row.createCell(cellNum++).setCellValue(geoClass.getName());
                row.createCell(cellNum++).setCellValue(geoClass.getCode());
            }
        }

        // Save the workbook to the system's documents directory
        String userHome = System.getProperty("user.home");
        String documentsPath = userHome + File.separator + "Documents" + File.separator + "sections.xlsx";
        try (FileOutputStream fileOut = new FileOutputStream(documentsPath)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    public void importSectionsFromExcel(MultipartFile file) throws IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        List<Section> sections = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            String sectionName = row.getCell(0).getStringCellValue();

            // Check if the section already exists in the database
            Section section = sectionRepository.findByName(sectionName)
                    .orElse(new Section());
            section.setName(sectionName);

            for (int j = 1; j < row.getLastCellNum(); j += 2) {
                String geoClassName = row.getCell(j).getStringCellValue();
                String geoClassCode = row.getCell(j + 1).getStringCellValue();

                boolean geoClassExists = section.getGeologicalClasses().stream()
                        .anyMatch(gc -> gc.getName().equals(geoClassName) && gc.getCode().equals(geoClassCode));
                if (!geoClassExists) {
                    GeologicalClass geoClass = new GeologicalClass();
                    geoClass.setName(geoClassName);
                    geoClass.setCode(geoClassCode);

                    // Add geological class to the section
                    section.getGeologicalClasses().add(geoClass);
                }
            }

            // Add section to the list
            sections.add(section);
        }

        // Save all sections to the database
        sectionRepository.saveAll(sections);
        workbook.close();
    }

    public JobStatus getJobStatus(Long jobId) {
        return jobStatusRepository.findById(jobId).orElse(null);
    }
}