pipeline {
    agent any
    environment{
        REPO = 'https://github.com/annadatska/bot'
        BRANCH = 'develop'
        DOCKER='datskadevops'
        DOCKERHUB_CREDENTIALS=credentials('dockerhub')
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
        stage('Login') {
			steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub', passwordVariable: 'DOCKERHUB_CREDENTIALS_PSW', usernameVariable: 'DOCKERHUB_CREDENTIALS_USR')]) {
                    withEnv(["DOCKERHUB_PASSWORD=${env.DOCKERHUB_CREDENTIALS_PSW}", "DOCKERHUB_USERNAME=${env.DOCKERHUB_CREDENTIALS_USR}"]) {
                        sh 'docker login -u $DOCKERHUB_USERNAME -p $DOCKERHUB_PASSWORD'
                    }
                }
            }
		}
        stage('push'){
            script{
                    docker.withRegistry('', 'dockerhub'){
                        sh 'make push'
                    }
                }
        }
    }

    post {
		always {
			sh 'docker logout'
		}
	}
}