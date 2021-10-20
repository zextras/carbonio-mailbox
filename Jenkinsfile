pipeline {
    parameters {
        booleanParam defaultValue: false, description: 'Whether to upload the packages in playground repositories', name: 'PLAYGROUND'
    }
    options {
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '5'))
        timeout(time: 3, unit: 'HOURS')
    }
    agent {
        node {
            label 'base-agent-v1'
        }
    }
    environment {
        NETWORK_OPTS = '--network ci_agent'
    }
    stages {
        stage('Fetch git-repo and sources') {
            agent {
                node {
                    label 'python3-agent-v1'
                }
            }
            steps {
                sh '''
curl https://storage.googleapis.com/git-repo-downloads/repo > repo
python3 repo init -u ssh://git@bitbucket.org/zextras/carbonio-ce -b ${BRANCH_NAME}
python3 repo sync -vc -j 5
'''
                stash includes: '**', name: 'project'
            }
        }
        stage('Build Carbonio CE') {
            agent {
                node {
                    label 'base-agent-v1'
                }
            }
            steps {
                checkout scm
                sh './build'
                unstash 'project'
                script {
                    env.CONTAINER_ID = sh(returnStdout: true, script: "docker run -dt ${NETWORK_OPTS} carbonio-builder").trim()
                }
                sh "docker cp ${WORKSPACE} ${env.CONTAINER_ID}:/src"
                sh "docker exec -t ${env.CONTAINER_ID} bash -c \"build_all.sh ${BUILD_NUMBER}\""
                sh "docker exec -t ${env.CONTAINER_ID} bash -c \"cp -r /src/carbonio-core /pkg\""
                sh 'mkdir staging'
                sh "docker cp ${env.CONTAINER_ID}:/pkg/. staging"
                stash includes: 'staging/**', name: 'staging'
            }
            post {
                always {
                    sh "docker kill ${env.CONTAINER_ID}"
                }
            }
        }
        stage('Packaging Carbonio') {
            parallel {
                stage('Ubuntu 18.04') {
                    agent {
                        node {
                            label 'pacur-agent-ubuntu-18.04-v1'
                        }
                    }
                    steps {
                        unstash 'staging'
                        sh 'cp -r staging/* /tmp'
                        sh 'sudo pacur build ubuntu-bionic /tmp/carbonio-core'
                        stash includes: 'artifacts/', name: 'artifacts-ubuntu-bionic'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'artifacts/*.deb', fingerprint: true
                        }
                    }
                }
                stage('Centos 8') {
                    agent {
                        node {
                            label 'pacur-agent-centos-8-v1'
                        }
                    }
                    steps {
                        unstash 'staging'
                        sh 'cp -r staging/* /tmp'
                        sh 'sudo pacur build centos /tmp/carbonio-core'
                        stash includes: 'artifacts/', name: 'artifacts-centos-8'
                    }
                    post {
                        always {
                            archiveArtifacts artifacts: 'artifacts/*.rpm', fingerprint: true
                        }
                    }
                }
            }
        }
        stage('Upload To Playground') {
            when {
                anyOf {
                    branch 'zextras/*'
                    expression { params.PLAYGROUND == true }
                }
            }
            steps {
                unstash 'artifacts-ubuntu-bionic'
                unstash 'artifacts-centos-8'
                script {
                    def server = Artifactory.server 'zextras-artifactory'
                    def buildInfo
                    def uploadSpec
                    buildInfo = Artifactory.newBuildInfo()
                    uploadSpec = '''{
                        "files": [
                            {
                                "pattern": "artifacts/*bionic*.deb",
                                "target": "ubuntu-playground/pool/",
                                "props": "deb.distribution=bionic;deb.component=main;deb.architecture=amd64"
                            },
                            {
                                "pattern": "artifacts/(carbonio-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-ce)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-core-jar)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-core-libs)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-conf)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-conf-attrs)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-conf-msgs)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-conf-rights)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-db)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-docs)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-common-mbox-native-lib)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-core)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-core-components)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-ldap)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-ldap-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-ldap-components)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-logger)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-admin-console-war)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-conf)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-service)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-store-libs)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-war)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mbox-webclient-war)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-memcached-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mta)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mta-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-mta-components)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-osl)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-os-requirements)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-perl)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-perl-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-proxy)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-proxy-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-proxy-components)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-server)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-store)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-store-base)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-store-components)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            },
                            {
                                "pattern": "artifacts/(carbonio-timezone-data)-(*).rpm",
                                "target": "centos8-playground/zextras/{1}/{1}-{2}.rpm",
                                "props": "rpm.metadata.arch=x86_64;rpm.metadata.vendor=zextras"
                            }
                        ]
                    }'''
                    server.upload spec: uploadSpec, buildInfo: buildInfo, failNoOp: false
                }
            }
        }
    }
}

