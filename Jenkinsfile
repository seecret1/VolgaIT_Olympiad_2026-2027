pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile'
            additionalBuildArgs '--pull'
            args '--shm-size=2g'
        }
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
    }

    stages {
        stage('Compile') {
            steps {
                sh './mvnw --batch-mode --no-transfer-progress -DskipTests test-compile'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw --batch-mode --no-transfer-progress clean test -Dheadless=true -Dbrowser=chrome'
            }
        }

        stage('Allure report') {
            steps {
                sh './mvnw --batch-mode --no-transfer-progress allure:report'
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/TEST-*.xml'
            archiveArtifacts allowEmptyArchive: true,
                    artifacts: 'target/allure-results/**,target/site/allure-maven-plugin/**,target/surefire-reports/**',
                    fingerprint: true
        }
        cleanup {
            deleteDir()
        }
    }
}
