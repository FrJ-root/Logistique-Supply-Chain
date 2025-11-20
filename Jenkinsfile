pipeline {
    agent any

    tools {
        jdk 'jdk21'
    }

    environment {
        JAVA_HOME = tool(name: 'jdk21', type: 'jdk')
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SONARQUBE = 'SonarServer'
        SONAR_TOKEN = credentials('sonar-token')
        MAVEN_OPTS = "-DskipTests=false"
        JACOCO_XML_PATH = "target/site/jacoco/jacoco.xml"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                echo "Building project and running tests..."
                sh './mvnw clean verify -DskipITs -B'
            }
            post {
                always {
                    echo "Archiving test results and code coverage..."
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: 'target/classes',
                        sourcePattern: 'src/main/java',
                        inclusionPattern: '**/*.class',
                        runAlways: true
                    )
                    archiveArtifacts artifacts: 'target/*.jar, target/**/*.xml, target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    echo "🔍 Running SonarQube analysis..."
                    withSonarQubeEnv("${SONARQUBE}") {
                        sh """
                            ./mvnw sonar:sonar \
                                -Dsonar.projectKey=Logistics-API \
                                -Dsonar.host.url=$SONAR_HOST_URL \
                                -Dsonar.token=$SONAR_TOKEN \
                                -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                                -Dsonar.coverage.exclusions=**/dto/**,**/mapper/**
                        """
                    }
                }
            }
        }

        stage('Quality Gate') {
            steps {
                script {
                    echo "⏳ Waiting for SonarQube Quality Gate..."
                    timeout(time: 10, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Quality Gate failed: ${qg.status}"
                        } else {
                            echo "Quality Gate passed."
                        }
                    }
                }
            }
        }

        stage('Package') {
            steps {
                echo "Packaging project..."
                sh './mvnw -DskipTests package -B'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Docker Image') {
            when { branch 'main' }
            steps {
                echo "Building Docker image..."
                sh 'docker build -t myregistry.example.com/logistics-api:${GIT_COMMIT} .'
            }
        }
    }

    post {
        failure {
            echo "Build failed - sending notifications..."
        }
        success {
            echo "Pipeline Success! Build ${env.BUILD_NUMBER}"
        }
    }
}