pipeline {
  agent any

  tools {
    jdk 'JDK17'
    maven 'Maven'
  }

  environment {
    SONARQUBE = 'SonarServer'              // name configured in Jenkins -> Configure System
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
      steps {
        timeout(time: 5, unit: 'MINUTES') {
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
        // If you mount docker socket into Jenkins container this will build
        sh 'docker build -t myregistry.example.com/logistics-api:${GIT_COMMIT} .'
        // optional: docker push ...
      }
    }
  }

  post {
    failure {
      echo "Build failed - send notification"
      // Add steps to notify Slack or Email here, example with slackSend if plugin configured
      // slackSend(channel: '#ci', color: 'danger', message: "Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}")
    }
    success {
      echo "Pipeline Success! Build ${env.BUILD_NUMBER}"
    }
  }
}