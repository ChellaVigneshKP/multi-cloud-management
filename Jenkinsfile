pipeline {
    agent any

    tools {
        sonarScanner 'SonarScanner'
    }

    options {
        buildDiscarder(logRotator(daysToKeepStr: '10', numToKeepStr: '10'))
        timeout(time: 15, unit: 'MINUTES')
        timestamps()
    }

    environment {
        SONARQUBE_ENV = 'sonar-server'
        FRONTEND_DIR = 'frontend'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
                echo '✅ Workspace cleaned.'
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
                echo '✅ Code checkout completed.'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir("${env.FRONTEND_DIR}") {
                    withSonarQubeEnv("${env.SONARQUBE_ENV}") {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                def scannerHome = tool 'SonarScanner'
                                if (isUnix()) {
                                    sh "${scannerHome}/bin/sonar-scanner"
                                } else {
                                    bat "${scannerHome}\\bin\\sonar-scanner.bat"
                                }
                            }
                        }
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
    }

    post {
        always {
            cleanWs()
            echo '🧹 Workspace cleaned after build.'
        }
        success {
            echo '✅ Pipeline completed successfully.'
        }
        failure {
            echo '❌ Pipeline failed. Check SonarQube and logs.'
        }
    }
}