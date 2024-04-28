pipeline {
    agent { label 'codespace' }
    parameters {
        choice(name: 'OS', choices: ['linux', 'darwin', 'windows', 'all'], description: 'Choose OS for container-image building')
        choice(name: 'TARGETARCH', choices: ['amd64', 'arm64'], description: 'Pick architecture')
    }
    environment {
        GIT_REPO = 'https://github.com/annadatska/bot'
        BRANCH = 'develop'
        REGISTRY = 'datskadevops'
        DOCKERHUB_CREDENTIALS = credentials('dockerhub')
    }
    stages {
        stage('clone') {
            steps {
                echo 'Clone repo'
                git branch: "${BRANCH}", url: "${GIT_REPO}"
            }
        }
        stage('Login to Docker Repository') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR')]) {
                    withEnv(["DOCKERHUB_PASSWORD=${env.DOCKERHUB_CREDENTIALS_PSW}", "DOCKERHUB_USERNAME=${env.DOCKERHUB_CREDENTIALS_USR}"]) {
                        sh 'docker login -u $DOCKERHUB_USERNAME -p $DOCKERHUB_PASSWORD'
                    }
                }
            }
        }
        stage('test') {
            steps {
                echo 'run tests from Makefile'
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
        stage('push') {
            parallel {
                stage('Push Linux architecture to repository') {
                    when { expression { params.OS == 'linux' || params.OS == 'all' } }
                    steps {
                        sh 'make push TARGETOS=linux'
                    }
                }
                stage('Push Darwin architecture to repository') {
                    when { expression { params.OS == 'darwin' || params.OS == 'all' } }
                    steps {
                        sh 'make push TARGETOS=macos'
                    }
                }
                stage('Push Windows architecture to repository') {
                    when { expression { params.OS == 'windows' || params.OS == 'all' } }
                    steps {
                        sh 'make push TARGETOS=windows'
                    }
                }
            }
        }
        stage('clean') {
            parallel {
                stage('Clean Linux image') {
                    when { expression { params.OS == 'linux' || params.OS == 'all' } }
                    steps {
                        sh 'make clean TARGETOS=linux'
                    }
                }
                stage('Clean Darwin image') {
                    when { expression { params.OS == 'darwin' || params.OS == 'all' } }
                    steps {
                        sh 'make clean TARGETOS=macos'
                    }
                }
                stage('Clean Windows image') {
                    when { expression { params.OS == 'windows' || params.OS == 'all' } }
                    steps {
                        sh 'make clean TARGETOS=windows'
                    }
                }
            }
        }
    }
}