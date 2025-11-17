pipeline {
  agent any

  tools {
    jdk 'JDK17'
    maven 'Maven'
  }

  environment {
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
          jacoco(execPattern: '**/target/jacoco.exec', classPattern: 'target/classes', sourcePattern: 'src/main/java', inclusionPattern: '**/*.class')
          archiveArtifacts artifacts: 'target/*.jar, target/**/*.xml, target/site/jacoco/**', allowEmptyArchive: true
        }
      }
    }

   stage('SonarQube Analysis') {
     steps {
       script {
         withSonarQubeEnv("${SONARQUBE}") {
           sh '''
             ./mvnw sonar:sonar \
               -Dsonar.projectKey=Logistics-API \
               -Dsonar.host.url=$SONAR_HOST_URL \
               -Dsonar.login=$SONAR_TOKEN \
               -Dsonar.coverage.jacoco.xmlReportPaths=$JACOCO_XML_PATH
           '''
         }
       }
     }
   }


    stage('Quality Gate') {
        tools {
            maven 'Maven'
            jdk 'JDK17'
        }
        environment {
            scannerHome = tool 'SonarScanner'
        }
        steps {
            timeout(time: 10, unit: 'MINUTES') {
                waitForQualityGate abortPipeline: true
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