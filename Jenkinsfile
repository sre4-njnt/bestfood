pipeline {
    agent any

    tools {
      // Jenkins 'Global Tool Configuration' 에 설정한 버전과 연동
      maven 'maven'
    }

    environment {
        ECR_PATH = '483106397391.dkr.ecr.ap-northeast-2.amazonaws.com'
        ECR_IMAGE = 'njnt-tomcat'
        REGION = 'ap-northeast-2'
        ACCOUNT_ID='483106397391'
        
        GIT_BRANCH = 'main'
        GIT_REPO_URL = 'https://github.com/gpfk1015/demo.git'

    }

    stages {
        stage('Git Clone from gitSCM') {
            steps {
                script {
                    try {
                        git branch: 'main', 
                            credentialsId: 'github-token',
                            url: 'https://github.com/gpfk1015/demo.git'
                        sh "ls -lat"
                        sh "sudo rm -rf ./.git"
                        env.cloneResult=true
                        
                    } catch (error) {
                        print(error)
                        env.cloneResult=false
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage("Build WAR with Maven") {
            when {
                expression {
                    return env.cloneResult ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/
                }                
            }
            steps {
                script{
                    try {
                        sh """
                        rm -rf deploy
                        mkdir deploy
                        mvn --version
                        java -version
                        """
                        sh "sed -i 's/  version:.*/  version: \${VERSION:v${env.BUILD_NUMBER}}/g' /var/jenkins_home/workspace/${env.JOB_NAME}/src/main/resources/application.yaml"
                        sh "cat /var/jenkins_home/workspace/${env.JOB_NAME}/src/main/resources/application.yaml"
                        sh 'mvn -e -Dmaven.test.failure.ignore=true clean package'
                        sh """
                        cd deploy
                        cp /var/jenkins_home/workspace/${env.JOB_NAME}/target/*.war ./${ECR_IMAGE}.war
                        """
                        env.mavenBuildResult=true
                    } catch (error) {
                        print(error)
                        echo 'Remove Deploy Files'
                        sh "sudo rm -rf /var/jenkins_home/workspace/${env.JOB_NAME}/*"
                        env.mavenBuildResult=false
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        stage('Docker Build and Push to ECR'){
            when {
                expression {
                    return env.mavenBuildResult ==~ /(?i)(Y|YES|T|TRUE|ON|RUN)/
                }
            }
            steps {
                script{
                    try {
                        sh"""
                        #!/bin/bash
                        cat>Dockerfile<<-EOF
# Tomcat 이미지를 사용합니다. 여기서는 Tomcat 9와 OpenJDK 17을 사용하는 버전을 선택합니다.
FROM tomcat:9.0.88-jdk17-temurin-focal

# Tomcat의 기본 설정을 변경하거나, 추가적인 설정 파일을 넣고 싶다면 여기서 COPY 또는 ADD 명령을 사용할 수 있습니다.
# 예: ADD 설정파일경로 /usr/local/tomcat/conf/

# 환경변수로 JAVA_OPTS를 설정합니다. 메모리 관리와 관련된 JVM 옵션을 여기에 추가할 수 있습니다.
ENV JAVA_OPTS="-XX:InitialRAMPercentage=40.0 -XX:MaxRAMPercentage=80.0"

# server.xml copy
COPY ./server.xml /usr/local/tomcat/conf/server.xml

# 애플리케이션 WAR 파일을 Tomcat의 webapps 폴더 아래에 배포합니다.
# ECR_IMAGE 환경변수를 사용하여 동적으로 WAR 파일 이름을 지정합니다.
ADD ./deploy/${ECR_IMAGE}.war /usr/local/tomcat/webapps/ROOT.war
#ADD ./deploy/${ECR_IMAGE}.war /usr/local/tomcat/webapps/${ECR_IMAGE}.war
#COPY /usr/local/tomcat/webapps/${ECR_IMAGE}.war /usr/local/tomcat/webapps/ROOT.war

# Tomcat이 사용하는 8080 포트를 노출합니다.
EXPOSE 8080

EOF"""
                        docker.withRegistry("https://${ECR_PATH}", "ecr:${REGION}:AWSCredentials") {
                            def image = docker.build("${ECR_PATH}/${ECR_IMAGE}:${env.BUILD_NUMBER}")
                            image.push()
                        }
                        
                        echo 'Remove Deploy Files'
                        sh "sudo rm -rf /var/jenkins_home/workspace/${env.JOB_NAME}/*"
                        env.dockerBuildResult=true
                    } catch (error) {
                        print(error)
                        echo 'Remove Deploy Files'
                        sh "sudo rm -rf /var/jenkins_home/workspace/${env.JOB_NAME}/*"
                        env.dockerBuildResult=false
                        currentBuild.result = 'FAILURE'
                    }
                }
            }
        }
        /*
        stage('Prepare deployment.yaml') {
            steps {
                script {
                    // deployment.yaml 파일 생성 또는 수정 작업
                    sh """
                    #!/bin/bash
                    cat>tomcat-deployment.yaml<<-EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tomcat-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: tomcat
  template:
    metadata:
      labels:
        app: tomcat
    spec:
      containers:
      - name: tomcat
        image: ${ECR_PATH}/${ECR_IMAGE}:${env.BUILD_NUMBER}
        ports:
        - containerPort: 8080
      imagePullSecrets:
      - name: ecr-njnt-secret
EOF"""
                }
            }
        }*/
        stage('Git Push') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'github-pat', variable: 'GITHUB_TOKEN')]) {
                    //gitUsernamePassword(credentialsId: 'github-token', gitToolName: 'Default') {

                        //sh "sed -i 's/tomcat:.*/tomcat:${currentBuild.number}/g' tomcat-deployment.yaml"
                        //sh 'git init'
                        //sh 'git add tomcat-deployment.yaml'
                        //sh "git commit -m 'fix: ${currentBuild.number} image versioning'"
                        //sh "git push https://github.com/gpfk1015/demo.git/tomcat-deployment.yaml main --force"
                        sh "git clone https://${GITHUB_TOKEN}@github.com/gpfk1015/demo.git"
 
                        dir('demo'){
                            sh 'git config --global user.email "gpfk1015@gmail.com"'
                            sh 'git config --global user.name "gpfk1015"'
                            sh "sed -i 's/tomcat:.*/tomcat:${currentBuild.number}/g' tomcat-deployment.yaml"
                            sh "git add tomcat-deployment.yaml"
                            sh "git commit -m 'fix: ${currentBuild.number} image versioning'"
                            //sh "git checkout -b main"
                            sh "git push https://gpfk1015:${GITHUB_TOKEN}@github.com/gpfk1015/demo.git main --force"
                        }
                        //sh "git branch -M main"
                        //sh "git remote remove origin"
                        //sh 'git remote add origin https://github.com/gpfk1015/demo.git'
                        //sh 'git remote set-url origin https://gpfk1015:${GITHUB_TOKEN}@github.com/gpfk1015/demo.git'
                        //sh 'git pull origin main'
                        //sh 'git push -u origin main'
                    }
                    
                }
            }
        }
    }
    
    post {
        success {
          echo 'Deploy Success'
          echo 'Remove Deploy Files'
          sh "sudo rm -rf /var/jenkins_home/workspace/${env.JOB_NAME}/*"
        }
        failure {
          echo 'Deploy Failure'
          echo 'Remove Deploy Files'
          sh "sudo rm -rf /var/jenkins_home/workspace/${env.JOB_NAME}/*"
        }
    }
    
}