pipeline {
    agent any

    options {
        buildDiscarder(logRotator(daysToKeepStr: '10', numToKeepStr: '10'))
        timeout(time: 15, unit: 'MINUTES')
    }

    environment {
        SONAR_SERVER = 'sonar-server'
    }

    stages {

        stage('Clean Workspace') {
            steps {
                cleanWs()
                echo '✅ Workspace cleaned before build.'
            }
        }

        stage('Checkout Code') {
            steps {
                script {
                    checkout scm
                    echo '✅ Code checked out successfully.'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv(SONAR_SERVER) {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            timeout(time: 10, unit: 'MINUTES') {
                                dir('frontend') {
                                    sonarScanner()
                                }
                            }
                        }
                    }
                    echo '✅ SonarQube analysis completed!'
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
            echo '✅ Build completed successfully!'
        }
        failure {
            echo '❌ Build failed. Check logs for details.'
        }
    }
}