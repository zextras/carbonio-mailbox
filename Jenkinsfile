library identifier: 'mailbox-packages-lib@master', retriever: modernSCM(
        [$class       : 'GitSCMSource',
         remote       : 'git@github.com:zextras/jenkins-packages-build-library.git',
         credentialsId: 'jenkins-integration-with-github-account'])

def mvnCmd(String cmd) {
    def profile = ''
    if (buildingTag()) {
        profile = '-Pprod'
    }
    else if (env.BRANCH_NAME == 'devel' ) {
        profile = '-Pdev'
    }
    sh 'mvn -B -s settings-jenkins.xml ' + profile + ' ' + cmd
}
def getPackages() {
    return ["carbonio-appserver-conf","carbonio-appserver-db", "carbonio-appserver-service",
            "carbonio-appserver-store-libs", "carbonio-appserver-war", "carbonio-common-appserver-conf",
            "carbonio-common-appserver-docs",
            "carbonio-common-appserver-native-lib", "carbonio-common-core-jar",
            "carbonio-common-core-libs", "carbonio-directory-server"]
}

pipeline {
    agent {
        node {
            label 'carbonio-agent-v1'
        }
    }
    parameters {
        booleanParam defaultValue: false, description: 'Upload packages in playground repositories.', name: 'PLAYGROUND'
        booleanParam defaultValue: false, description: 'Skip test and sonar analysis.', name: 'SKIP_TEST_WITH_COVERAGE'
        booleanParam defaultValue: false, description: 'Skip sonar analysis.', name: 'SKIP_SONARQUBE'
    }
    environment {
        JAVA_OPTS='-Dfile.encoding=UTF8'
        LC_ALL='C.UTF-8'
        MAVEN_OPTS = "-Xmx4g"
        BUILD_PROPERTIES_PARAMS='-Ddebug=0 -Dis-production=1'
        GITHUB_BOT_PR_CREDS = credentials('jenkins-integration-with-github-account')
    }
    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        timeout(time: 2, unit: 'HOURS')
        skipDefaultCheckout()
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                    sh "cp ${SETTINGS_PATH} settings-jenkins.xml"
                }
            }
        }
        stage("Generate SBOM and submit"){
            when {
                anyOf {
                    branch 'main'
                }
            }
            steps{
                sh '''
                    curl -sSfL https://raw.githubusercontent.com/anchore/syft/main/install.sh | sh -s -- -b .
                    ./syft . -o cyclonedx-json=sbom.cyclonedx.json
                '''
                dependencyTrackPublisher artifact: 'sbom.cyclonedx.json',
                        synchronous: false,
                        projectName: 'carbonio-mailbox',
                        projectVersion: 'rc'
            }
        }
        stage('Build') {
            steps {
                mvnCmd("$BUILD_PROPERTIES_PARAMS -DskipTests=true clean install")

                sh 'mkdir staging'

                sh 'cp -r store* milter* native client common packages soap carbonio-jetty-libs staging'
                stash includes: 'staging/**', name: 'staging'

            }
        }
        stage('UT & IT') {
            when {
                expression {
                    params.SKIP_TEST_WITH_COVERAGE == false
                }
            }
            steps {

                mvnCmd("$BUILD_PROPERTIES_PARAMS test -Dexcludegroups=api")
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            }
        }
        stage('API Testing') {
            when {
                expression {
                    params.SKIP_TEST_WITH_COVERAGE == false
                }
            }
            steps {
                mvnCmd("$BUILD_PROPERTIES_PARAMS test -Dgroups=api")
            }
        }
        stage('Publish Coverage') {
            when {
                expression {
                    params.SKIP_TEST_WITH_COVERAGE == false
                }
            }
            steps {
                recordCoverage(tools: [[parser: 'JACOCO']],sourceCodeRetention: 'MODIFIED')
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            }
        }
        stage('Sonarqube Analysis') {
            when {
                allOf {
                    expression { params.SKIP_SONARQUBE == false }
                    expression { params.SKIP_TEST_WITH_COVERAGE == false }
                }
            }
            steps {
                withSonarQubeEnv(credentialsId: 'sonarqube-user-token', installationName: 'SonarQube instance') {
                    mvnCmd("$BUILD_PROPERTIES_PARAMS sonar:sonar")
                }
            }
        }
        stage('Publish SNAPSHOT to maven') {
            when {
                branch 'devel';
            }
            steps {
                mvnCmd('$BUILD_PROPERTIES_PARAMS deploy -DskipTests=true -Pdev')
            }
        }
        stage('Publish to maven') {
            when {
                buildingTag()
            }
            steps {
                mvnCmd('$BUILD_PROPERTIES_PARAMS deploy -DskipTests=true -Pprod')
            }
        }
        stage('Packages') {
            steps {
                script {
                    // packages, staging directory, relative path directory to yap.json
                    buildStage(getPackages(), 'staging', 'packages', params.PLAYGROUND).call()
                }
            }
        }
    }
}
