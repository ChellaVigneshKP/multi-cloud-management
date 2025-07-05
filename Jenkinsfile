pipeline {
    agent any

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
            }
        }

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir(env.FRONTEND_DIR) {
                    withSonarQubeEnv(env.SONARQUBE_ENV) {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                if (isUnix()) {
                                    sh 'sonar-scanner'
                                } else {
                                    bat 'sonar-scanner'
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
        }
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed. Check SonarQube and logs.'
        }
    }
}
