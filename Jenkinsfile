def mvnCmd(String cmd) {
  sh 'mvn -B -s settings-jenkins.xml ' + cmd
}
pipeline {
    agent {
        node {
        label 'carbonio-agent-v1'
        }
    }
    parameters {
        booleanParam defaultValue: false, description: 'Whether to upload the packages in playground repositories', name: 'PLAYGROUND'
        booleanParam defaultValue: false, description: 'Bump version + CHANGELOG + commit + push + tag', name: 'TAG'
        booleanParam defaultValue: false, description: 'Whether to skip the test all with coverage stage', name: 'SKIP_TEST_WITH_COVERAGE'
    }
    environment {
        JAVA_OPTS='-Dfile.encoding=UTF8'
        LC_ALL='C.UTF-8'
        BUILD_PROPERTIES_PARAMS='-Ddebug=0 -Dis-production=1'
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
            stage('Build') {
            steps {

                mvnCmd("$BUILD_PROPERTIES_PARAMS -DskipTests=true clean install")

                sh 'mkdir staging'

                sh 'cp -r store* milter* native client common packages soap staging'
                stash includes: 'staging/**', name: 'staging'

            }
        }
        stage('Test all with coverage (allow failure)') {
            when {
                expression {
                params.SKIP_TEST_WITH_COVERAGE == false
                }
            }
            steps {

                mvnCmd("$BUILD_PROPERTIES_PARAMS test")

                publishCoverage adapters: [jacocoAdapter(mergeToOneReport: true, path: '**/target/site/jacoco/jacoco.xml')], calculateDiffForChangeRequests: true, failNoReports: true
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
            }
        }
        stage('Publish SNAPSHOT to maven') {
              when {
                anyOf {
                  branch 'devel';
                  branch 'feature/maven-build';
                  expression{env.CHANGE_BRANCH == 'feature/maven-build'}
                }
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
        stage('Build deb/rpm') {
            stages {
                stage('pacur') {
                parallel {
                    stage('Ubuntu 20.04') {
                    agent {
                        node {
                        label 'pacur-agent-ubuntu-20.04-v1'
                        }
                    }
                    steps {
                        unstash 'staging'
                        sh 'cp -r staging /tmp'
                        sh 'sudo pacur build ubuntu-focal /tmp/staging/packages'
                        stash includes: 'artifacts/', name: 'artifacts-ubuntu-focal'
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
                        label 'pacur-agent-rocky-8-v1'
                        }
                    }
                    steps {
                        unstash 'staging'
                        sh 'cp -r staging /tmp'
                        sh 'sudo pacur build rocky-8 /tmp/staging/packages'
                        stash includes: 'artifacts/', name: 'artifacts-rocky-8'
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
        stage("Tag") {
            when {
                allOf {
                    expression { params.TAG == true }
                    branch 'main'
                }
            }
            steps {
                withCredentials([gitUsernamePassword(credentialsId: 'jenkins-integration-with-github-account',
                        gitToolName: 'git-tool')]) {
                    sh 'git config user.name $GITHUB_BOT_PR_CREDS_USR'
                    sh 'git config user.email bot@zextras.com'
                    sh 'git config user.password $GITHUB_BOT_PR_CREDS_PSW'
                    sh 'git checkout main' //avoid detached head
                    sh 'release-it --ci'
                }
            }
        }
        stage('Upload To Devel') {
            when {
                anyOf {
                    branch 'devel'
                }
            }
            steps {
                unstash 'artifacts-ubuntu-focal'
                unstash 'artifacts-rocky-8'

                    script {
                    def server = Artifactory.server 'zextras-artifactory'
                    def buildInfo
                    def uploadSpec
                    buildInfo = Artifactory.newBuildInfo()
                    uploadSpec ='''{
                        "files": [{
                            "pattern": "artifacts/*focal*.deb",
                            "target": "ubuntu-devel/pool/",
                            "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-conf)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-service)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-war)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-conf)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-db)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-docs)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-native-lib)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-core-jar)-(*).rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                }
            }
        }
        stage('Upload To Playground') {
            when {
                anyOf {
                    expression {
                        params.PLAYGROUND == true
                    }
                }
            }
            steps {
                unstash 'artifacts-ubuntu-focal'
                unstash 'artifacts-rocky-8'

                script {
                    def server = Artifactory.server 'zextras-artifactory'
                    def buildInfo
                    def uploadSpec
                    buildInfo = Artifactory.newBuildInfo()
                    uploadSpec ='''{
                        "files": [{
                            "pattern": "artifacts/*focal*.deb",
                            "target": "ubuntu-playground/pool/",
                            "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-conf)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-service)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-war)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-conf)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-db)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-docs)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-native-lib)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-core-jar)-(*).rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                }
            }
        }
        stage('Upload & Promotion Config') {
                when {
                    anyOf {
                        branch 'release/*'
                        buildingTag()
                    }
                }
                steps {
                    unstash 'artifacts-ubuntu-focal'
                    unstash 'artifacts-rocky-8'

                    script {
                        def server = Artifactory.server 'zextras-artifactory'
                        def buildInfo
                        def uploadSpec
                        def config

                        //ubuntu
                        buildInfo = Artifactory.newBuildInfo()
                        buildInfo.name += '-ubuntu'
                        uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/*focal*.deb",
                            "target": "ubuntu-rc/pool/",
                            "props": "deb.distribution=focal;deb.component=main;deb.architecture=amd64"
                        }]
                        }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                    config = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'sourceRepo': 'ubuntu-rc',
                        'targetRepo': 'ubuntu-release',
                        'comment': 'Do not change anything! Just press the button',
                        'status': 'Released',
                        'includeDependencies': false,
                        'copy': true,
                        'failFast': true
                    ]
                    Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: "Ubuntu Promotion to Release"
                    server.publishBuildInfo buildInfo

                    //rocky8
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name += '-centos8'
                    uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/(carbonio-appserver-conf)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-service)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-appserver-war)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-conf)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-db)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-docs)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-appserver-native-lib)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/(carbonio-common-core-jar)-(*).rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                    config = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'sourceRepo': 'centos8-rc',
                        'targetRepo': 'centos8-release',
                        'comment': 'Do not change anything! Just press the button',
                        'status': 'Released',
                        'includeDependencies': false,
                        'copy': true,
                        'failFast': true
                    ]
                    Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: 'Centos8 Promotion to Release'
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }
}
