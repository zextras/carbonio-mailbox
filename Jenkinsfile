def lib = library identifier: 'mailbox-packages-lib@chore/improvements-pt2', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'git@github.com:zextras/jenkins-packages-build-library.git',
         credentialsId: 'jenkins-integration-with-github-account'])
def mvnCmd(String cmd) {
    def profile = ''
    if (isBuildingTag()) {
        profile = '-Pprod'
    }
    else if (env.BRANCH_NAME == 'devel' ) {
        profile = '-Pdev'
    }
    sh 'mvn -B -s settings-jenkins.xml ' + profile + ' ' + cmd
}
def isBuildingTag() {
    if (env.TAG_NAME) {
        return true
    }
    return false
}

def buildContainer(String title, String description, String dockerfile, String tag) {
    sh 'docker build ' +
            '--label org.opencontainers.image.title="' + title + '" ' +
            '--label org.opencontainers.image.description="' + description + '" ' +
            '--label org.opencontainers.image.vendor="Zextras" ' +
            '-f ' + dockerfile + ' -t ' + tag + ' .'
    sh 'docker push ' + tag
}

def getPackages() {
    return ["carbonio-appserver-conf","carbonio-appserver-db",
            "carbonio-appserver-service", "carbonio-common-appserver-conf",
            "carbonio-common-appserver-native-lib", "carbonio-directory-server",
            "carbonio-mailbox-jar"
    ]
}


pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }

    triggers {
        cron(env.BRANCH_NAME == 'devel' ? 'H 5 * * *' : '')
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
                    sh 'cp ${SETTINGS_PATH} settings-jenkins.xml'
                }
                script {
                    env.GIT_COMMIT = sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
                }
            }
        }
        stage('Build') {
            steps {
                container('jdk-17') {
                    sh 'apt update && apt install -y build-essential'
                    mvnCmd("$BUILD_PROPERTIES_PARAMS -DskipTests=true clean install")
                    sh 'mkdir staging'
                    sh 'cp -r store* milter* native client common packages soap jython-libs staging'
                    script {
                        if (BRANCH_NAME == 'devel') {
                            def packages = getPackages()
                            def timestamp = new Date().format('yyyyMMddHHmmss')
                            packages.each { packageName ->
                                def cleanPackageName = packageName.replaceFirst(/^carbonio-/, '')
                                sh "sed -i \"s!pkgrel=.*!pkgrel=${timestamp}!\" staging/packages/${cleanPackageName}/PKGBUILD"
                            }
                        }
                    }
                    stash includes: 'staging/**', name: 'staging'
                }
            }
        }
        stage('UT, IT & API tests') {
            when {
                expression {
                    params.SKIP_TEST_WITH_COVERAGE == false
                }
            }
            steps {
                container('jdk-17') {
                    mvnCmd("$BUILD_PROPERTIES_PARAMS verify")
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
                }
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
                container('jdk-17') {
                    withSonarQubeEnv(credentialsId: 'sonarqube-user-token', installationName: 'SonarQube instance') {
                        mvnCmd("$BUILD_PROPERTIES_PARAMS sonar:sonar -Dsonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports -Dsonar.exclusions=**/com/zimbra/soap/mail/type/*.java,**/com/zimbra/soap/mail/message/*.java,**/com/zimbra/cs/account/ZAttr*.java,**/com/zimbra/common/account/ZAttr*.java")
                    }
                }
            }
        }
        stage('Publish containers - devel') {
            when {
                branch 'devel';
            }
            steps {
                container('dind') {
                    withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
                        buildContainer('Carbonio Mailbox', '$(cat docker/standalone/mailbox/description.md)',
                                'docker/standalone/mailbox/Dockerfile', 'registry.dev.zextras.com/dev/carbonio-mailbox:latest')
                        buildContainer('Carbonio MariaDB', '$(cat docker/standalone/mariadb/description.md)',
                                'docker/standalone/mariadb/Dockerfile', 'registry.dev.zextras.com/dev/carbonio-mariadb:latest')
                        buildContainer('Carbonio OpenLDAP', '$(cat docker/standalone/openldap/description.md)',
                                'docker/standalone/openldap/Dockerfile', 'registry.dev.zextras.com/dev/carbonio-openldap:latest')
                    }
                }
            }
        }
        stage('Publish SNAPSHOT to maven') {
            when {
                branch 'devel';
            }
            steps {
                container('jdk-17') {
                    mvnCmd('$BUILD_PROPERTIES_PARAMS deploy -DskipTests=true')
                }
            }
        }
        stage('Publish to maven') {
            when {
                expression {
                    return isBuildingTag()
                }
            }
            steps {
                container('jdk-17') {
                    mvnCmd('$BUILD_PROPERTIES_PARAMS deploy -DskipTests=true')
                }
            }
        }
        stage ('Build Packages') {
            steps {
                script {
                    buildStage(getPackages(), 'staging', 'packages')()
                }
            }
        }
    }
}
