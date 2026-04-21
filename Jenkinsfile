library(
        identifier: 'jenkins-lib-common@1.5.0',
        retriever: modernSCM([
                $class: 'GitSCMSource',
                credentialsId: 'jenkins-integration-with-github-account',
                remote: 'git@github.com:zextras/jenkins-lib-common.git',
        ])
)

properties(defaultPipelineProperties())

boolean isBuildingTag() {
    return env.TAG_NAME ? true : false
}

String profile = isBuildingTag() ? '-Pprod' :
        (env.BRANCH_NAME == 'devel' ? '-Pdev' : '')

pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }

    environment {
        MVN_OPTS = "-Ddebug=0 -Dis-production=1 ${profile}"
        GITHUB_BOT_PR_CREDS = credentials('jenkins-integration-with-github-account')
        JAVA_OPTS = '-Dfile.encoding=UTF8'
        LC_ALL = 'C.UTF-8'
        MAVEN_OPTS = '-Xmx2g'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
    }

    triggers {
        cron(env.BRANCH_NAME == 'devel' ? 'H 5 * * *' : '')
    }

    stages {
        stage('Setup') {
            steps {
                checkout scm
                script {
                    gitMetadata()
                }
            }
        }

        stage('Build') {
            parallel {
                stage('Maven build') {
                    steps {
                        container('jdk-21') {
                            sh """
                        mvn ${MVN_OPTS} \
                            -DskipTests=true \
                            clean install
                        mkdir staging
                        cp -a store* right-manager \
                                client common packages soap \
                                staging/
                    """
                            stash includes: 'staging/**', name: 'staging'
                        }
                    }
                }
            }
        }

        stage('UT, IT') {
            steps {
                container('jdk-21') {
                    sh "mvn ${MVN_OPTS} jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -DexcludedGroups=api,flaky,e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }
        stage('Flaky, API, E2E tests') {
                    steps {
                        container('jdk-21') {
                            sh "cd store && mvn ${MVN_OPTS} jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=flaky,api && mvn ${MVN_OPTS} jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=e2e"
                        }
                        junit allowEmptyResults: true,
                                testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
                    }
                }

        stage('Build and Package API Docs') {
            steps {
                container('jdk-21') {
                    sh """
                (
                    cd soap || { echo "Directory soap does not exist"; exit 1; }
                    mvn ${MVN_OPTS} antrun:run@generate-soap-docs
                )
                VERSION=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                mkdir -p docs
                tar -czf docs/carbonio-mailbox-api-docs-\${VERSION}.tar.gz -C soap/target/docs/soap .
            """
                }
                archiveArtifacts artifacts: 'docs/carbonio-mailbox-api-docs-*.tar.gz', allowEmptyArchive: true
            }
        }

        stage('Sonarqube Analysis') {
            steps {
                container('jdk-21') {
                    withSonarQubeEnv(credentialsId: 'sonarqube-user-token', installationName: 'SonarQube instance') {
                        sh """
                            mvn ${MVN_OPTS} \
                                sonar:sonar \
                                -Dsonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports \
                                -Dsonar.exclusions=**/com/zimbra/soap/mail/type/*.java,**/com/zimbra/soap/mail/message/*.java,**/com/zimbra/cs/account/ZAttr*.java,**/com/zimbra/common/account/ZAttr*.java
                        """
                    }
                }
            }
        }

        stage('Build and upload artifacts')
        {
            parallel {
                stage('Packages') {
                    stages {
                        stage('Build deb/rpm') {
                            steps {
                                echo 'Building deb/rpm packages'
                                buildStage([
                                        addCarbonioRepos: true,
                                        carbonioRepoCredentialId: 'artifactory-jenkins-gradle-properties-splitted',
                                        skipStash: true,
                                        buildDirs: ['staging/packages'],
                                ])
                            }
                        }
                        stage ('Publish packages') {
                            tools {
                                jfrog 'jfrog-cli'
                            }
                            steps {
                                uploadStage(
                                        packages: yapHelper.getPackageNames('staging/packages/yap.json')
                                )
                            }
                        }
                    }
                }

                stage('Publish SNAPSHOT to maven') {
                    when {
                        allOf {
                            not { buildingTag() }
                            branch 'devel'
                        }

                    }
                    steps {
                        container('jdk-21') {
                            withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                                script {
                                    sh "mvn ${MVN_OPTS} -s " + SETTINGS_PATH + " deploy -DskipTests=true"
                                }
                            }
                        }
                    }
                }

                stage('Publish to maven') {
                    when {
                        buildingTag()
                    }
                    steps {
                        container('jdk-21') {
                            withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                                script {
                                    sh "mvn ${MVN_OPTS} -s " + SETTINGS_PATH + " deploy -Dchangelist= -DskipTests=true"
                                }
                            }
                        }
                    }
                }

                stage('Build and Publish Docker images') {
                    steps {
                        dockerStage([
                                dockerfile: 'docker/mailbox/Dockerfile',
                                imageName : 'carbonio-mailbox',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title          : 'Carbonio Mailbox',
                                        descriptionFile: 'docker/mailbox/description.md'
                                ]
                        ])
                        dockerStage([
                                dockerfile: 'docker/mailbox-sidecar/Dockerfile',
                                imageName : 'carbonio-mailbox-sidecar',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title : 'Carbonio Mailbox Sidecar',
                                ]
                        ])
                        dockerStage([
                                dockerfile: 'docker/mailbox-admin-sidecar/Dockerfile',
                                imageName : 'carbonio-mailbox-admin-sidecar',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title : 'Carbonio Mailbox Admin Sidecar',
                                ]
                        ])
                        dockerStage([
                                dockerfile: 'docker/mailbox-nslookup-sidecar/Dockerfile',
                                imageName : 'carbonio-mailbox-nslookup-sidecar',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title : 'Carbonio Mailbox NSLookup Sidecar',
                                ]
                        ])
                        dockerStage([
                                dockerfile: 'docker/mailbox-internal-api-sidecar/Dockerfile',
                                imageName : 'carbonio-mailbox-internal-api-sidecar',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title : 'Carbonio Mailbox Internal API Sidecar',
                                ]
                        ])
                        dockerStage([
                                dockerfile: 'docker/mariadb/Dockerfile',
                                imageName : 'carbonio-mariadb',
                                platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                ocLabels  : [
                                        title          : 'Carbonio MariaDB',
                                        descriptionFile: 'docker/mariadb/description.md'
                                ]
                        ])
                    }
                }
            }

        }
        stage('Bump version and tag') {
            when {
                anyOf {
                    branch 'main'
                    branch 'devel'
                }
            }
            steps {
                script {
                    dt2_semanticRelease()
                }
            }
        }
    }
}
