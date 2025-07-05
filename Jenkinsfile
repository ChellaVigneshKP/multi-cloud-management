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

        stage('Install Dependencies') {
            steps {
                dir(env.FRONTEND_DIR) {
                    script {
                        if (isUnix()) {
                            sh 'npm ci'
                        } else {
                            bat 'npm ci'
                        }
                    }
                }
            }
        }

        stage('Run Tests with Coverage') {
            steps {
                dir(env.FRONTEND_DIR) {
                    script {
                        if (isUnix()) {
                            sh 'npm run test'
                        } else {
                            bat 'npm run test'
                        }
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                dir(env.FRONTEND_DIR) {
                    withSonarQubeEnv(env.SONARQUBE_ENV) {
                        catchError(buildResult: 'UNSTABLE', stageResult: 'FAILURE') {
                            script {
                                if (isUnix()) {
                                    sh 'npx sonar-scanner'
                                } else {
                                    bat 'npx sonar-scanner'
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
            echo '✅ Pipeline completed successfully.'
        }
        failure {
            echo '❌ Pipeline failed. Check SonarQube and logs.'
        }
    }
}