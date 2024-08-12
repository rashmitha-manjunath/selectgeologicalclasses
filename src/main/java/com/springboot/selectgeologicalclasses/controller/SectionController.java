package com.springboot.selectgeologicalclasses.controller;
import com.springboot.selectgeologicalclasses.model.JobStatus;
import com.springboot.selectgeologicalclasses.model.Section;
import com.springboot.selectgeologicalclasses.repositories.SectionRepository;
import com.springboot.selectgeologicalclasses.service.ExcelService;
import com.springboot.selectgeologicalclasses.service.SectionService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/sections")
public class SectionController {
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private ExcelService excelService;

    @GetMapping
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @GetMapping("/code")
    public ResponseEntity<List<Section>> getSectionsByGeologicalClassCode(@RequestParam String code) {
        List<Section> sections = sectionRepository.findByGeologicalClassCode(code);
        return ResponseEntity.ok(sections);
    }

    @PostMapping
    public ResponseEntity<String> createSection(@RequestBody Section section) {
        if (sectionRepository.findByName(section.getName()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Section with name " + section.getName() + " already exists.");
        }
        sectionRepository.save(section);
        return ResponseEntity.ok("Section created successfully.");
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSection(@PathVariable Long id) {
        sectionRepository.deleteById(id);
        return ResponseEntity.ok("Section with ID " + id + " has been deleted successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateSection(@PathVariable Long id, @RequestBody Section sectionDetails) {
        return sectionService.updateSection(id, sectionDetails);
    }

    @GetMapping("/export")
    public ResponseEntity<Long> exportSectionsToExcel() throws ExecutionException, InterruptedException, IOException {
        Future<Long> jobIdFuture = excelService.exportSectionsToExcelAsync();
        Long jobId = jobIdFuture.get();
        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<String> getJobStatus(@PathVariable Long id) {
        JobStatus jobStatus = excelService.getJobStatus(id);
        if (jobStatus == null || !"EXPORT".equals(jobStatus.getJobType())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job ID not found");
        }
        return ResponseEntity.ok(jobStatus.getStatus());
    }

    @GetMapping("/export/{id}/file")
    public ResponseEntity<byte[]> getExportedFile(@PathVariable Long id) throws IOException {
        return sectionService.getExportedFile(id);
    }

    @PostMapping("/import")
    public ResponseEntity<Long> importSectionsFromExcel(@RequestParam("file") MultipartFile file) throws IOException, ExecutionException, InterruptedException {
        Future<Long> jobIdFuture = excelService.importSectionsFromExcelAsync(file);
        Long jobId = jobIdFuture.get();
        return ResponseEntity.ok(jobId);
    }

    @GetMapping("/import/{id}")
    public ResponseEntity<String> getImportJobStatus(@PathVariable Long id) {
        JobStatus jobStatus = excelService.getJobStatus(id);
        if (jobStatus == null || !"IMPORT".equals(jobStatus.getJobType())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job ID not found");
        }
        return ResponseEntity.ok(jobStatus.getStatus());
    }
}
