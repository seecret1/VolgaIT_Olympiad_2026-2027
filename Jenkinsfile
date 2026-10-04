pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile'
            additionalBuildArgs "--pull --build-arg BROWSER=${params.BROWSER}"
            args '--shm-size=2g'
        }
    }

    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds(abortPrevious: true)
    }

    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox', 'edge'],
                description: 'Browser used by the Selenium test suite')
    }

    stages {
        stage('Compile') {
            steps {
                sh './mvnw --batch-mode --no-transfer-progress -DskipTests test-compile'
            }
        }

        stage('Test') {
            steps {
                sh "./mvnw --batch-mode --no-transfer-progress clean test -Dheadless=true -Dbrowser=${params.BROWSER}"
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
