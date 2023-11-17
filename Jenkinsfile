def mvnCmd(String cmd) {
    def profile = ''
    if (env.BRANCH_NAME == 'main' ) {
        profile = '-Pprod'
    }
    else if (env.BRANCH_NAME == 'devel' ) {
        profile = '-Pdev'
    }
    sh 'mvn -B -s settings-jenkins.xml ' + profile + ' ' + cmd
}
def buildDebPackages(String flavor) {
    unstash 'staging'
    sh 'cp -r staging /tmp'
    sh 'sudo yap build ' + flavor + ' /tmp/staging/packages'
    stash includes: 'artifacts/*.deb', name: 'artifacts-' + flavor
}
def buildRpmPackages(String flavor) {
    unstash 'staging'
    sh 'cp -r staging /tmp'
    sh 'sudo yap build ' + flavor + ' /tmp/staging/packages'
    stash includes: 'artifacts/x86_64/*.rpm', name: 'artifacts-' + flavor
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

                publishCoverage adapters: [jacocoAdapter(mergeToOneReport: true, path: '**/target/site/jacoco/jacoco.xml')], calculateDiffForChangeRequests: true, failNoReports: true
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
                publishCoverage adapters: [jacocoAdapter(mergeToOneReport: true, path: '**/target/site/jacoco/jacoco.xml')], calculateDiffForChangeRequests: true, failNoReports: true
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                mvnCmd("$BUILD_PROPERTIES_PARAMS test -Dgroups=api")
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
        stage('Build deb/rpm') {
            stages {
                stage('yap') {
                parallel {
                    stage('Ubuntu 20.04') {
                    agent {
                        node {
                        label 'yap-agent-ubuntu-20.04-v2'
                        }
                    }
                    steps {
                        buildDebPackages("ubuntu-focal")
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
                        label 'yap-agent-ubuntu-22.04-v2'
                        }
                    }
                    steps {
                        buildDebPackages("ubuntu-jammy")
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
                        label 'yap-agent-rocky-8-v2'
                        }
                    }
                    steps {
                        buildRpmPackages("rocky-8")
                    }
                    post {
                        always {
                        archiveArtifacts artifacts: 'artifacts/x86_64/*.rpm', fingerprint: true
                        }
                    }
                    }
                    stage('Rocky 9') {
                    agent {
                        node {
                        label 'yap-agent-rocky-9-v2'
                        }
                    }
                    steps {
                        buildRpmPackages("rocky-9")
                    }
                    post {
                        always {
                        archiveArtifacts artifacts: 'artifacts/x86_64/*.rpm', fingerprint: true
                        }
                    }
                    }
                }
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
                unstash 'artifacts-ubuntu-jammy'
                unstash 'artifacts-rocky-8'
                unstash 'artifacts-rocky-9'

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
                            "pattern": "artifacts/*jammy*.deb",
                            "target": "ubuntu-devel/pool/",
                            "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-devel/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }
                        ]
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
                unstash 'artifacts-ubuntu-jammy'
                unstash 'artifacts-rocky-8'
                unstash 'artifacts-rocky-9'

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
                            "pattern": "artifacts/*jammy*.deb",
                            "target": "ubuntu-playground/pool/",
                            "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-playground/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }
                        ]
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
                    unstash 'artifacts-ubuntu-jammy'
                    unstash 'artifacts-rocky-8'
                    unstash 'artifacts-rocky-9'

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
                        },
                        {
                            "pattern": "artifacts/*jammy*.deb",
                            "target": "ubuntu-rc/pool/",
                            "props": "deb.distribution=jammy;deb.component=main;deb.architecture=amd64"
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

                    //rhel9
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name += '-centos8'
                    uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el8.x86_64.rpm",
                            "target": "centos8-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }
                        ]
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

                    //rhel9
                    buildInfo = Artifactory.newBuildInfo()
                    buildInfo.name += '-rhel9'
                    uploadSpec = '''{
                        "files": [{
                            "pattern": "artifacts/x86_64/(carbonio-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-db)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-service)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-war)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-conf)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-db)-(*).el8.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el8.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-docs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-appserver-native-lib)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-jar)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-appserver-store-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        },
                        {
                            "pattern": "artifacts/x86_64/(carbonio-common-core-libs)-(*).el9.x86_64.rpm",
                            "target": "rhel9-rc/zextras/{1}/{1}-{2}.el9.x86_64.rpm",
                            "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                        }
                        ]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                    config = [
                        'buildName': buildInfo.name,
                        'buildNumber': buildInfo.number,
                        'sourceRepo': 'rhel9-rc',
                        'targetRepo': 'rhel9-release',
                        'comment': 'Do not change anything! Just press the button',
                        'status': 'Released',
                        'includeDependencies': false,
                        'copy': true,
                        'failFast': true
                    ]
                    Artifactory.addInteractivePromotion server: server, promotionConfig: config, displayName: 'RHEL9 Promotion to Release'
                    server.publishBuildInfo buildInfo
                }
            }
        }
    }
}
