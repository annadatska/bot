pipeline {
    agent any
    environment {
        REPO = 'https://github.com/annadatska/bot'
        BRANCH = 'develop'
        GITHUB_TOKEN = credentials('annadatska')
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
        stage('test') {
            steps {
                echo "Test Build"
                sh 'make test'
            }
        }
        stage('build'){
            steps{
                script{
                    echo "Start build application"
                    if (params.OS == "linux" && params.ARCH == "amd64"){
                        sh 'make linux'
                    }
                    else if (params.OS == "linux" && params.ARCH == "arm64"){
                        sh 'make linux arm'
                    }
                    else if (params.OS == "windows" && params.ARCH == "amd64"){
                        sh 'make windows'
                    }
                    else if (params.OS == "windows" && params.ARCH == "arm64"){
                        echo "Sorry, ARM is not supported for windows"
                    }
                    else if (params.OS == "darwin"){
                        echo "Sorry, OS is not supported"
                    }
                }
            }
        }

        stage('image') {
            steps {
                echo "Building image for platform ${params.OS} on ${params.ARCH} started"
                sh "make image"
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
