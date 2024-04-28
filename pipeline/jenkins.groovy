pipeline {
    agent any
    environment{
        REPO = 'https://github.com/annadatska/bot'
        BRANCH = 'develop'
        GITHUB_TOKEN=credentials('annadatska')
    }
    parameters {
        choice(name: 'OS', choices: ['linux', 'darwin', 'windows', 'all'], description: 'Pick OS')
        choice(name: 'ARCH', choices: ['amd64', 'arch64'], description: 'Pick Arch')

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
                        sh 'make image TARGETOS=linux TARGETARCH=${params.ARCH}'
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
        stage('login to GHCR') {
            steps {
                sh "echo $GITHUB_TOKEN_PSW | docker login ghcr.io -u $GITHUB_TOKEN_USR --password-stdin"
            }
        }
        stage('push image') {
            steps {
                sh "make -n ${params.OS} ${params.ARCH} image push"
            }
        } 
    }
}