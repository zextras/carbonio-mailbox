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

def buildPackages(String flavor, String ext) {
    container('yap') {
        unstash 'staging'
        sh 'cp -r staging /tmp'
        script {
            if (BRANCH_NAME == 'devel') {
                def timestamp = new Date().format('yyyyMMddHHmmss')
                sh 'sudo yap build ' + flavor + ' /tmp/staging/packages -r ${timestamp}'
            } else {
                sh 'sudo yap build ' + flavor + ' /tmp/staging/packages'
            }
        }
        stash includes: 'artifacts/*.' + ext, name: 'artifacts-' + flavor
    }
}

def getPackages() {
    return ["carbonio-appserver-conf","carbonio-appserver-db",
            "carbonio-appserver-service", "carbonio-common-appserver-conf",
            "carbonio-common-appserver-native-lib", "carbonio-directory-server",
            "carbonio-mailbox-jar"
    ]
}
def getRpmSpec(String upstream, String version) {
    packages = getPackages()
    packageSpecList = []
    filesSpec = ""
    packages.each { item ->
        packageSpecList.add(generateRpmSpec(item, version, upstream))
    }
    return packageSpecList.join(",")
}

def generateRpmSpec(String packageName, String version, String upstream) {
    return """{
        "pattern": "artifacts/(${packageName})-(*).el${version}.x86_64.rpm",
        "target": "${upstream}/zextras/{1}/{1}-{2}.el${version}.x86_64.rpm",
        "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras;vcs.revision=${env.GIT_COMMIT}"
    }
    """
}

pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }

    tools {
        jfrog 'jfrog-cli'
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
                    sh 'cp $SETTINGS_PATH settings-jenkins.xml'
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
                    stash includes: 'staging/**', name: 'staging'
                }
            }
        }
        stage('Audit') {
            steps {
                container('jfrog') {
                    jf 'audit --mvn --iac --sca --secrets --sast'
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
            stages {
                stage('yap') {
                    parallel {
                        stage('Ubuntu 20.04') {
                            agent {
                                node {
                                    label 'yap-ubuntu-20-v1'
                                }
                            }
                            steps {
                                buildPackages("ubuntu-focal", "deb")
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*focal*.deb', fingerprint: true
                                }
                            }
                        }
                        stage('Ubuntu 22.04') {
                            agent {
                                node {
                                    label 'yap-ubuntu-22-v1'
                                }
                            }
                            steps {
                                buildPackages("ubuntu-jammy", "deb")
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.deb', fingerprint: true
                                }
                            }
                        }
                        stage('Ubuntu 24.04') {
                            agent {
                                node {
                                    label 'yap-ubuntu-24-v1'
                                }
                            }
                            steps {
                                buildPackages("ubuntu-noble", "deb")
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.deb', fingerprint: true
                                }
                            }
                        }
                        stage('Rocky 8') {
                            agent {
                                node {
                                    label 'yap-rocky-8-v1'
                                }
                            }
                            steps {
                                buildPackages("rocky-8", "rpm")
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.rpm', fingerprint: true
                                }
                            }
                        }
                        stage('Rocky 9') {
                            agent {
                                node {
                                    label 'yap-rocky-9-v1'
                                }
                            }
                            steps {
                                buildPackages("rocky-9", "rpm")
                            }
                            post {
                                always {
                                    archiveArtifacts artifacts: 'artifacts/*.rpm', fingerprint: true
                                }
                            }
                        }
                    }
                }
            }
        }
        // stage('Upload To Devel') {
        //     when {
        //         anyOf {
        //             branch 'devel'
        //         }
        //     }
        //     steps {
        //         unstash 'artifacts-ubuntu-focal'
        //         unstash 'artifacts-ubuntu-jammy'
        //         unstash 'artifacts-ubuntu-noble'
        //         unstash 'artifacts-rocky-8'
        //         unstash 'artifacts-rocky-9'

        //         script {
        //             def uploadSpec
        //             uploadSpec ="""{
        //                 "files": [{
        //                     "pattern": "artifacts/*focal*.deb",
        //                     "target": "ubuntu-devel/pool/",
        //                     "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*jammy*.deb",
        //                     "target": "ubuntu-devel/pool/",
        //                     "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*noble*.deb",
        //                     "target": "ubuntu-devel/pool/",
        //                     "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },""" + getRpmSpec("centos8-devel", "8") + """,""" + getRpmSpec("rhel9-devel", "9") + """
        //                 ]
        //             }"""
        //         }
        //     }
        // }
        // stage('Upload To Playground') {
        //     when {
        //         anyOf {
        //             expression {
        //                 params.PLAYGROUND == true
        //             }
        //         }
        //     }
        //     steps {
        //         unstash 'artifacts-ubuntu-focal'
        //         unstash 'artifacts-ubuntu-jammy'
        //         unstash 'artifacts-ubuntu-noble'
        //         unstash 'artifacts-rocky-8'
        //         unstash 'artifacts-rocky-9'

        //         script {
        //             def uploadSpec
        //             uploadSpec ="""{
        //                 "files": [{
        //                     "pattern": "artifacts/*focal*.deb",
        //                     "target": "ubuntu-playground/pool/",
        //                     "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*jammy*.deb",
        //                     "target": "ubuntu-playground/pool/",
        //                     "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*noble*.deb",
        //                     "target": "ubuntu-playground/pool/",
        //                     "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 """ + getRpmSpec("centos8-playground", "8") + """,""" + getRpmSpec("rhel9-playground", "9") + """]
        //             }"""
        //         }
        //     }
        // }
        // stage('Upload & Promotion Config') {
        //     when {
        //         expression {
        //             return isBuildingTag()
        //         }
        //     }
        //     steps {
        //         unstash 'artifacts-ubuntu-focal'
        //         unstash 'artifacts-ubuntu-jammy'
        //         unstash 'artifacts-ubuntu-noble'
        //         unstash 'artifacts-rocky-8'
        //         unstash 'artifacts-rocky-9'

        //         script {
        //             def config
        //             def uploadSpec
        //             //ubuntu
        //             uploadSpec = """{
        //                 "files": [{
        //                     "pattern": "artifacts/*focal*.deb",
        //                     "target": "ubuntu-rc/pool/",
        //                     "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*jammy*.deb",
        //                     "target": "ubuntu-rc/pool/",
        //                     "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 },
        //                 {
        //                     "pattern": "artifacts/*noble*.deb",
        //                     "target": "ubuntu-rc/pool/",
        //                     "props": "deb.distribution=noble;deb.component=main;deb.architecture=amd64;vcs.revision=${env.GIT_COMMIT}"
        //                 }]
        //                 }"""
        //             config = [
        //                     'buildName': buildInfo.name,
        //                     'buildNumber': buildInfo.number,
        //                     'sourceRepo': 'ubuntu-rc',
        //                     'targetRepo': 'ubuntu-release',
        //                     'comment': 'Do not change anything! Just press the button',
        //                     'status': 'Released',
        //                     'includeDependencies': false,
        //                     'copy': true,
        //                     'failFast': true
        //             ]
        //             Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Ubuntu Promotion to Release"
        //             //centos8
        //             uploadSpec = """{
        //                 "files": [""" + getRpmSpec("centos8-rc", "8") + """]
        //             }"""
        //             config = [
        //                     'buildName': buildInfo.name,
        //                     'buildNumber': buildInfo.number,
        //                     'sourceRepo': 'centos8-rc',
        //                     'targetRepo': 'centos8-release',
        //                     'comment': 'Do not change anything! Just press the button',
        //                     'status': 'Released',
        //                     'includeDependencies': false,
        //                     'copy': true,
        //                     'failFast': true
        //             ]
        //             Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: 'Centos8 Promotion to Release'

        //             //rhel9
        //             uploadSpec = """{
        //                 "files": [""" + getRpmSpec("rhel9-rc", "9") + """
        //                 ]
        //             }"""
        //             config = [
        //                     'buildName': buildInfo.name,
        //                     'buildNumber': buildInfo.number,
        //                     'sourceRepo': 'rhel9-rc',
        //                     'targetRepo': 'rhel9-release',
        //                     'comment': 'Do not change anything! Just press the button',
        //                     'status': 'Released',
        //                     'includeDependencies': false,
        //                     'copy': true,
        //                     'failFast': true
        //             ]
        //             Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: 'RHEL9 Promotion to Release'
        //         }
        //     }
        // }
    }
}
