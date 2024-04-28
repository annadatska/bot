pipeline {
    agent any
    environment{
        REPO = 'https://github.com/annadatska/bot'
        BRANCH = 'develop'
        DOCKER='datskadevops'
    }
    parameters {
        choice(name: 'OS', choices: ['linux', 'darwin', 'windows', 'all'], description: 'Pick OS')
        choice(name: 'ARCH', choices: ['amd64', 'arch64'], description: 'Pick ARCH')

    }
    stages {
        stage('clone') {
            steps {
                echo "Clone repo"
                git branch: "${BRANCH}", url: "${REPO}"
            }
        }
        stage('test'){
            steps{
                echo "Test Build"
                sh 'make test'
            }
        }
        stage('build') {
            parallel {
                stage('Build for Linux platform') {
                    when { expression { params.OS == 'linux' || params.OS == 'all' } }
                    steps {
                        echo 'Building for Linux platform'
                        sh 'make image TARGETOS=linux TARGETARCH=${TARGETARCH}'
                    }
                }
                stage('Build Darwin for Darwin platform') {
                    when { expression { params.OS == 'darwin' || params.OS == 'all' } }
                    steps {
                        echo 'Building for Darwin platform'
                        sh 'make image TARGETOS=macos'
                    }
                }
                stage('Build for Windows platform') {
                    when { expression { params.OS == 'windows'  || params.OS == 'all' } }
                    steps {
                        echo 'Building for Windows'
                        sh 'make image TARGETOS=windows'
                    }
                }
            }
        }
        stage('push'){
            steps{
                script{
                    docker.withRegistry('', 'dockerhub'){
                        sh 'make push'
                    }
                }
            }
        }
    }
}