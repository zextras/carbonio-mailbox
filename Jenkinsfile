library(
    identifier: 'jenkins-packages-build-library@1.0.1',
    retriever: modernSCM([
        $class: 'GitSCMSource',
        remote: 'git@github.com:zextras/jenkins-packages-build-library.git',
        credentialsId: 'jenkins-integration-with-github-account'
    ])
)

def mvnCmd(String cmd) {
    def profile = ''
    if (isBuildingTag()) {
        profile = '-Pprod'
    }
    else if (env.BRANCH_NAME == 'devel' ) {
        profile = '-Pdev'
    }
    sh "mvn -B -Dmaven.repo.local=${env.WORKSPACE}/.m2/repository -ntp -s settings-jenkins.xml ${profile} ${cmd}"
}
def isBuildingTag() {
    if (env.TAG_NAME) {
        return true
    }
    return false
}

pipeline {

    agent {
        node {
            label 'zextras-v1'
        }
    }

    environment {
        BUILD_PROPERTIES_PARAMS='-Ddebug=0 -Dis-production=1'
        GITHUB_BOT_PR_CREDS = credentials('jenkins-integration-with-github-account')
        JAVA_OPTS='-Dfile.encoding=UTF8'
        LC_ALL='C.UTF-8'
        MAVEN_OPTS = "-Xms2g -Xmx4g"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        parallelsAlwaysFailFast()
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
    }

    parameters {
        booleanParam defaultValue: false, 
            description: 'Upload packages in playground repositories.', 
            name: 'PLAYGROUND'
        booleanParam defaultValue: false, 
            description: 'Skip test and sonar analysis.', 
            name: 'SKIP_TEST_WITH_COVERAGE'
        booleanParam defaultValue: false, 
            description: 'Skip sonar analysis.', 
            name: 'SKIP_SONARQUBE'
    }

    tools {
        jfrog 'jfrog-cli'
    }

    triggers {
        cron(env.BRANCH_NAME == 'devel' ? 'H 5 * * *' : '')
    }


    stages {
        stage('Show Workspace') {
            steps {
                echo "Workspace is: ${env.WORKSPACE}"
            }
        }
        
        stage('Checkout') {
            steps {
                checkout scm
                withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                    sh 'cp ${SETTINGS_PATH} settings-jenkins.xml'
                }
                script {
                    gitMetadata()
                }
            }
        }

        stage('Build') {
            steps {
                container('jdk-17') {
                    sh 'apt update && apt install -y build-essential'
                    mvnCmd("$BUILD_PROPERTIES_PARAMS -DskipTests=true clean install")
                    sh 'mkdir staging'
                    sh 'cp -r store* milter* attribute-manager right-manager mailbox-ldap-lib native client common packages soap jython-libs staging'
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

        stage('Publish containers') {
            when {
                expression {
                    return isBuildingTag() || env.BRANCH_NAME == 'devel'
                }
            }
            steps {
                container('dind') {
                    withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
                        script {
                            def tagVersions = []
                            if (isBuildingTag()) {
                                tagVersions = [env.TAG_NAME, "stable"]
                            } else {
                                tagVersions = ["devel", "latest"]
                            }
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/mailbox/Dockerfile',
                                imageName: 'registry.dev.zextras.com/dev/carbonio-mailbox',
                                imageTags: tagVersions,
                                ocLabels: [
                                    title: 'Carbonio Mailbox',
                                    descriptionFile: 'docker/standalone/mailbox/description.md',
                                    version: env.GIT_TAG ? env.GIT_TAG : "stable"
                                ]
                            ])
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/mariadb/Dockerfile',
                                imageName: 'registry.dev.zextras.com/dev/carbonio-mariadb',
                                imageTags: tagVersions,
                                ocLabels: [
                                    title: 'Carbonio MariaDB',
                                    descriptionFile: 'docker/standalone/mariadb/description.md',
                                    version: env.GIT_TAG ? env.GIT_TAG : "stable"
                                ]
                            ])
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/openldap/Dockerfile',
                                imageName: 'registry.dev.zextras.com/dev/carbonio-openldap',
                                imageTags: tagVersions,
                                ocLabels: [
                                    title: 'Carbonio OpenLDAP',
                                    descriptionFile: 'docker/standalone/openldap/description.md',
                                    version: env.GIT_TAG ? env.GIT_TAG : "stable"
                                ]
                            ])
                        }
                    }
                    withDockerRegistry(
                        credentialsId: 'private-registry', 
                        url: 'https://registry.dev.zextras.com'
                    ) {
                        script {
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/mailbox/Dockerfile', 
                                imageName: 'registry.dev.zextras.com/dev/carbonio-mailbox',
                                ocLabels: [
                                    title: 'Carbonio Mailbox', 
                                    descriptionFile: 'docker/standalone/mailbox/description.md',
                                    version: 'latest'
                                ]
                            ])
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/mariadb/Dockerfile', 
                                imageName: 'registry.dev.zextras.com/dev/carbonio-mariadb',
                                ocLabels: [
                                    title: 'Carbonio MariaDB', 
                                    descriptionFile: 'docker/standalone/mariadb/description.md',
                                    version: 'latest'
                                ]
                            ])
                            dockerHelper.buildImage([
                                dockerfile: 'docker/standalone/openldap/Dockerfile', 
                                imageName: 'registry.dev.zextras.com/dev/carbonio-openldap',
                                ocLabels: [
                                    title: 'Carbonio OpenLDAP', 
                                    descriptionFile: 'docker/standalone/openldap/description.md',
                                    version: 'latest'
                                ]
                            ])
                        }
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

        stage('Build deb/rpm') {
            steps {
                echo "Building deb/rpm packages"
                buildStage([
                    skipStash: true,
                    buildDirs: ['staging/packages'],
                    overrides: [
                        'ubuntu': [
                            preBuildScript: '''
                                apt-get update 
                                apt-get install -y --no-install-recommends rsync
                            '''
                        ]
                    ]
                ])
            }
        }

        stage('Upload artifacts')
        {
            steps {
                uploadStage(
                    packages: yapHelper.getPackageNames('staging/packages/yap.json')
                )
            }
        }
    }
}
