#Sections and GeologicalClasses

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher
- MySQL database

Configure the database: Update the application.properties file located in src/main/resources with your MySQL database credentials.
<pre>spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name spring.datasource.username=your_username spring.datasource.password=your_password spring.jpa.hibernate.ddl-auto=update </pre>

Build the project:  <pre>./gradlew build </pre> 

Run the application:  <pre>./gradlew bootRun </pre>

API Endpoints :

Get all sections:  <pre>GET /sections </pre>

Get sections by geological class code:  <pre>GET /sections/code?code={code} </pre>

Create a new section:  <pre>POST /sections </pre>

Update a section:  <pre>PUT /sections/{id} </pre>

Delete a section:  <pre>DELETE /sections/{id} </pre>

Export sections to Excel:  <pre>GET /sections/export </pre>

Get export job status:  <pre>GET /sections/export/{id} </pre>

Download exported file:  <pre>GET /sections/export/{id}/file </pre>

Import sections from Excel:  <pre>POST /sections/import </pre>

Get import job status:  <pre>GET /sections/import/{id} </pre>

Code Flow :

Main Components
Controller Layer:  
SectionController: Handles HTTP requests and maps them to service layer methods.

Service Layer:  
SectionService: Contains business logic for managing sections.
SectionExportService: Handles the export of sections to an Excel file.

Repository Layer:  
SectionRepository: Provides CRUD operations for Selection entities.
Model Layer:  
Selection: Represents a section entity.
JobStatus: Represents the status of an export/import job.

Detailed Code Flow :

Update Section:  
SectionController calls sectionService.updateSection(id, sectionDetails).
SectionService retrieves the section by ID, updates its details, and saves it back to the repository.

Export Sections to Excel:  
SectionController calls excelService.exportSectionsToExcelAsync().
ExcelService handles the asynchronous export process and returns a job ID.
The job status can be checked using excelService.getJobStatus(id).
The exported file can be downloaded using sectionExportService.getExportedFile(id).

Import Sections from Excel:
SectionController calls excelService.importSectionsFromExcelAsync(file).
ExcelService handles the asynchronous import process and returns a job ID.
The job status can be checked using excelService.getJobStatus(id).

Additional Information :

Exception Handling: Custom exceptions like ResourceNotFoundException are used to handle specific error cases.
Asynchronous Processing: Export and import operations are handled asynchronously to improve performance and user experience.