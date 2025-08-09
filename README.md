BillingWeb Frontend
Overview
This is the frontend for the BillingWeb application, a Java-based web interface for managing billing operations, developed by Bimsara Nirmal, a software engineering undergraduate at ICBT Campus, affiliated with Cardiff Metropolitan University. Built with Maven, this project provides a user-friendly interface to interact with the billing services backend through RESTful APIs.
Project Structure
frontend/
├── .github/
│   └── workflows/
├── billingWeb/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   ├── com/mycompany/billingweb/resources/
│   │   │   ├── resources/META-INF/
│   │   │   └── webapp/WEB-INF/
│   │   └── target/
│   │       ├── billingWeb-1.0-SNAPSHOT/
│   │       │   ├── META-INF/
│   │       │   └── WEB-INF/
│   │       │       ├── classes/
│   │       │       │   ├── com/mycompany/billingweb/resources/
│   │       │       │   └── META-INF/
│   │       ├── classes/
│   │       │   ├── com/mycompany/billingweb/resources/
│   │       │   └── META-INF/
│   │       ├── endorsed/
│   │       ├── generated-sources/annotations/
│   │       ├── maven-archiver/
│   │       └── maven-status/maven-compiler-plugin/

Directory Breakdown

src/main/java: Core Java source code
com/mycompany/billingweb/resources/: Java classes for handling frontend logic and API interactions


src/main/resources/META-INF: Configuration files for the application
src/main/webapp/WEB-INF: Web application configuration, including JSPs, servlets, or other web resources
target: Build output, including compiled classes and the WAR file
.github/workflows: GitHub Actions for CI/CD pipelines

Prerequisites

Java 17 or later
Maven 3.6+
A compatible servlet container (e.g., GlassFish Server)
Access to the billingServices backend API (or equivalent) for data integration

Setup Instructions

Clone the Repository:
git clone https://github.com/bimsaranirmal/BillingWeb.git
cd billingWeb


Configure Backend API:

Update API endpoint configurations (e.g., in src/main/resources/META-INF or relevant configuration files) to point to the backend service (e.g., http://localhost:8080/billingServices).
Ensure the backend service is running and accessible.


Build the Project:
mvn clean install


Run the Application:

Deploy the generated WAR file from target/billingWeb-1.0-SNAPSHOT.war to your servlet container.
Alternatively, use Maven to run locally:mvn tomcat7:run




Access the Application:

Open http://localhost:8080/billingWeb in your browser to view the frontend interface.



CI/CD

GitHub Actions workflows are defined in .github/workflows/ for automated building and deployment.

About the Author
Bimsara Nirmal is a software engineering undergraduate at ICBT Campus, affiliated with Cardiff Metropolitan University. Passionate about software development, quality assurance, and web development, Bimsara specializes in .NET technologies, API development, and mobile applications. He is open to collaboration on ASP.NET and web projects and can be reached at bimsaranirmal123@gmail.com. Explore more of his work on GitHub: bimsaranirmal.
Contributing

Fork the repository.
Create a feature branch (git checkout -b feature/your-feature).
Commit your changes (git commit -m 'Add your feature').
Push to the branch (git push origin feature/your-feature).
Open a Pull Request.

