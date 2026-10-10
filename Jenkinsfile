pipeline {
    agent any

    environment {
        // Docker configuration
        REGISTRY = 'streetlight/fault-reporting'
        IMAGE_NAME = '${REGISTRY}'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
        DOCKER_CRED_ID = 'docker-hub-credentials'

        // Application configuration
        APP_NAME = 'streetlight-fault-reporting'
        MAVEN_OPTS = '-Dspring.profiles.active=test'

        // Quality gate flags
        DEPLOY_ENABLED = true
    }

    stages {
        stage('1. Checkout') {
            steps {
                git branch: env.GIT_BRANCH, url: env.GIT_REPO_URL
            }
        }

        stage('2. Environment and Java Version Verification') {
            steps {
                sh 'java -version'
                sh './mvnw --version'
                env.MAVEN_OPTS ? 'MAVEN_OPTS set' : warning 'MAVEN_OPTS not set'
            }
        }

        stage('3. Maven Clean and Compile') {
            steps {
                sh './mvnw clean compile -B'
            }
        }

        stage('4. Unit Tests') {
            steps {
                sh './mvnw test -B -Dtest="*Test"'
            }
        }

        stage('5. Integration Tests') {
            steps {
                sh './mvnw verify -B -Dspring.profiles.active=test'
            }
        }

        stage('6. Package the Application') {
            steps {
                sh './mvnw package -B'
            }
        }

        stage('7. Archive the JAR Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/streetlight-*.jar', allowEmptyArchive: true
            }
        }

        stage('8. Build a Docker Image') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CRED_ID, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                    sh "docker login -u ${DOCKER_USER} -p ${DOCKER_PASS} ${REGISTRY}"
                    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    sh "docker push ${IMAGE_NAME}:${IMAGE_TAG}"
                }
            }
        }

        stage('9. Run Selenium Tests against Deployed App') {
            steps {
                // Deploy test app via Docker Compose
                sh '''
                    docker compose -f docker-compose.test.yml up -d
                    sleep 30
                    ./mvnw selenium:test -B
                '''
            }
        }

        stage('10. Publish Test Reports') {
            steps {
                junit '**/surefire-reports/*.xml'
                archiveArtifacts artifacts: 'target/selenium-reports/**', allowEmptyArchive: true
            }
        }

        stage('11. Deploy Only If Quality Gates Pass') {
            when {
                expression { return currentBuild.result == 'SUCCESS' }
            }
            steps {
                withCredentials([usernamePassword(credentialsId: env.DOCKER_CRED_ID, passwordVariable: 'DOCKER_PASS', usernameVariable: 'DOCKER_USER')]) {
                    sh "docker compose -f docker-compose.yml up -d"
                }
            }
        }

        stage('12. Deployment Health Check') {
            steps {
                script {
                    for (int i = 0; i < 30; i++) {
                        def health = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health", returnStdout: true).trim()
                        if (health == "200") {
                            echo "Application is healthy (HTTP 200)"
                            break
                        }
                        echo "Attempt ${i+1}: Application not ready, waiting 5s..."
                        sleep(5)
                    }
                    if (i >= 30) {
                        error "Health check failed after 150 seconds - initiating rollback"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed"
            archiveArtifacts artifacts: 'screenshots/**', allowEmptyArchive: true
        }
    }
}