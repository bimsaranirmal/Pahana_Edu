# Pahana_Edu Backend

## Overview
This is the backend for **Pahana_Edu**, an educational platform designed to manage educational resources, user data, and related services. Developed by **Bimsara Nirmal**, a software engineering undergraduate at ICBT Campus (affiliated with Cardiff Metropolitan University), this Java-based backend uses Maven for build management and provides RESTful APIs for seamless integration with front-end applications.

## Project Structure
```
Pahana_Edu/
├── .github/
│   └── workflows/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/pahanaedu/resources/
│   │   │   │   ├── DAO/
│   │   │   │   ├── Model/
│   │   │   │   └── Utils/
│   │   │   ├── resources/META-INF/
│   │   │   └── webapp/WEB-INF/
│   │   └── test/
│   │       └── java/DAO/
│   └── target/
│       ├── pahanaedu-1.0-SNAPSHOT/
│       │   ├── META-INF/
│       │   ├── WEB-INF/
│       │   │   ├── classes/
│       │   │   │   ├── com/pahanaedu/resources/
│       │   │   │   ├── DAO/
│       │   │   │   ├── Model/
│       │   │   │   └── Utils/
│       │   │   └── lib/
│       ├── classes/
│       ├── endorsed/
│       ├── generated-sources/annotations/
│       ├── generated-test-sources/test-annotations/
│       ├── maven-archiver/
│       ├── maven-status/maven-compiler-plugin/
│       ├── surefire-reports/
│       └── test-classes/DAO/
```

### Directory Breakdown
- **src/main/java**: Core Java source code
  - `com/pahanaedu/resources/`: REST API resources and controllers for educational services
  - `DAO/`: Data Access Objects for database interactions
  - `Model/`: Data models for entities like users, courses, and resources
  - `Utils/`: Utility classes for common functionality
- **src/main/resources/META-INF**: Configuration files (e.g., persistence settings)
- **src/main/webapp/WEB-INF**: Web application configuration
- **src/test/java/DAO**: Unit tests for the DAO layer
- **target**: Build output, including compiled classes, WAR file, and test reports
- **.github/workflows**: GitHub Actions for CI/CD pipelines

## Prerequisites
- Java 17 or later
- Maven 3.6+
- A compatible servlet container (e.g., GlassFish Server)
- Database (e.g., MySQL) configured for the application

## Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/bimsaranirmal/Pahana_Edu.git -b backend
   cd Pahana_Edu
   ```

2. **Configure Database**:
   - Update database connection settings in `src/main/resources/META-INF/persistence.xml` or equivalent configuration file.
   - Ensure the database is running and accessible.

3. **Build the Project**:
   ```bash
   mvn clean install
   ```

4. **Run the Application**:
   - Deploy the generated WAR file from `target/pahanaedu-1.0-SNAPSHOT.war` to your servlet container.
   - Alternatively, use Maven to run locally:
     ```bash
     mvn tomcat7:run
     ```

5. **Access the Application**:
   - Open `http://localhost:8080/pahanaedu` in your browser or use an API client (e.g., Postman) to interact with the REST endpoints.

## Testing
- Run unit tests:
  ```bash
  mvn test
  ```
- Test reports are generated in `target/surefire-reports/`.

## CI/CD
- GitHub Actions workflows are defined in `.github/workflows/` for automated building, testing, and deployment.

## About the Author
**Bimsara Nirmal** is a software engineering undergraduate at ICBT Campus, affiliated with Cardiff Metropolitan University. Passionate about software development, quality assurance, and web development, Bimsara specializes in .NET technologies, API development, and mobile applications. He is open to collaboration on ASP.NET and web projects and can be reached at **bimsaranirmal123@gmail.com**. Explore more of his work on GitHub: [bimsaranirmal](https://github.com/bimsaranirmal).

## Contributing
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a Pull Request.

## License
This project is licensed under the MIT License.
