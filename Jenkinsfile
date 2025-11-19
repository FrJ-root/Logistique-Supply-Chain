pipeline {
  agent any

  tools {
      jdk 'jdk21'
  }

  environment {
      JAVA_HOME = tool name: 'jdk21', type: 'jdk'
      PATH = "${JAVA_HOME}/bin:${env.PATH}"
      SONARQUBE = 'SonarServer'
      SONAR_TOKEN = credentials('sonar-token')
      MAVEN_OPTS = "-DskipTests=false"
      JACOCO_XML_PATH = "target/site/jacoco/jacoco.xml"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh './mvnw clean verify -DskipITs -B'
      }
      post {
          always {
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
               withSonarQubeEnv("${SONARQUBE}") {
                   sh """
                       ./mvnw sonar:sonar \
                           -Dsonar.projectKey=Logistics-API \
                           -Dsonar.host.url=$SONAR_HOST_URL \
                           -Dsonar.login=$SONAR_TOKEN \
                           -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                           -Dsonar.coverage.exclusions=**/dto/**,**/mapper/**
                   """
               }
           }
       }
   }

    stage('Quality Gate') {
        steps {
            script {
                timeout(time: 10, unit: 'MINUTES') {
                    def qg = waitForQualityGate()
                    if (qg.status != 'OK') {
                        error "Pipeline aborted due to quality gate failure: ${qg.status}"
                    }
                }
            }
        }
    }

    stage('Package') {
      steps {
        sh './mvnw -DskipTests package -B'
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Build Docker Image') {
      when { branch 'main' }
      steps {
        sh 'docker build -t myregistry.example.com/logistics-api:${GIT_COMMIT} .'
      }
    }
  }

  post {
    failure {
      echo "Build failed - send notification"
    }
    success {
      echo "Pipeline Success! Build ${env.BUILD_NUMBER}"
    }
  }
}