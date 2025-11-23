# CI/CD Pipeline Documentation

This section describes the Continuous Integration (CI) and Continuous Deployment (CD) setup for the Logistics API project, covering tools, configuration, and best practices.

---

## Tools Used

* **Jenkins**: Orchestrates the CI/CD pipeline.
* **Maven Wrapper (`./mvnw`)**: Builds, tests, and packages the Java Spring Boot project.
* **SonarQube**: Performs static code analysis for quality and security.
* **JaCoCo**: Measures test code coverage.
* **Docker**: Builds and pushes containerized application images.
* **Git**: Version control, integrated with Jenkins for automated builds on commit.

---

## Jenkins Pipeline (`Jenkinsfile`)

The pipeline is defined using a declarative `Jenkinsfile`. Key stages include:

1. **Checkout**

    * Pulls the source code from the repository.

   ```groovy
   checkout scm
   ```

2. **Build**

    * Runs Maven to compile the project and resolve dependencies.

   ```groovy
   sh './mvnw clean compile'
   ```

3. **Test**

    * Executes unit tests and generates test reports.

   ```groovy
   sh './mvnw test'
   junit '**/target/surefire-reports/*.xml'
   ```

4. **Static Code Analysis (SonarQube)**

    * Runs SonarQube analysis to check for bugs, vulnerabilities, and code smells.

   ```groovy
   withSonarQubeEnv('SonarQube') {
       sh './mvnw sonar:sonar'
   }
   ```

5. **Code Coverage (JaCoCo)**

    * Generates test coverage reports.

   ```groovy
   sh './mvnw jacoco:report'
   ```

6. **Docker Build & Push**

    * Builds the Docker image and pushes it to a registry.

   ```groovy
   sh 'docker build -t myregistry/logistics-api:${env.BUILD_NUMBER} .'
   sh 'docker push myregistry/logistics-api:${env.BUILD_NUMBER}'
   ```

7. **Deploy (Optional)**

    * Deploys the Docker container to staging or production environments.

---

## Pipeline Overview

```text
Git Commit
   │
   ▼
Jenkins Checkout
   │
   ▼
Maven Build & Compile
   │
   ▼
Unit Tests & JaCoCo
   │
   ▼
SonarQube Analysis
   │
   ▼
Docker Build & Push
   │
   ▼
Deployment
```

---

## Best Practices

* **Version Control Integration**

    * Trigger Jenkins builds automatically on every commit or pull request.
* **Fail Fast**

    * Fail the pipeline immediately if compilation or tests fail.
* **Code Quality**

    * SonarQube must pass quality gates before proceeding.
* **Immutable Builds**

    * Docker images should be tagged with the build number for traceability.
* **Environment Parity**

    * Ensure that local, staging, and production environments are consistent using Docker.

---

## Notes

* The CI/CD setup focuses on automated builds, testing, and code quality verification.
* Deployment can be integrated with Kubernetes, AWS ECS, or any container orchestration platform.
* Extend the pipeline to include database migrations, smoke tests, and rollback strategies.