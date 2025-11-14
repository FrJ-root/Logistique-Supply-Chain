pipeline {
    agent any

    tools {
        jdk 'JDK17'
        maven 'Maven'
    }

    environment {
        SONAR_SERVER = 'SonarServer'
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/your-repo/logistics-api.git'
            }
        }

        stage('Build & Test') {
            steps {
                sh './mvnw clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarServer') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=logistics-api'
                }
            }
        }

        stage("Quality Gate") {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Package') {
            steps {
                sh './mvnw package -DskipTests'
            }
        }

        // Optional Docker
        stage('Build Docker Image') {
            when { branch 'main' }
            steps {
                sh 'docker build -t logistics-api:latest .'
            }
        }
    }

    post {
        success {
            echo "Pipeline Success! 🚀"
        }
        failure {
            echo "Pipeline Failed ❌"
        }
    }
}
