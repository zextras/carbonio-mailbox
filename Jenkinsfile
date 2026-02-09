library(
        identifier: 'jenkins-lib-common@1.1.2',
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
        MAVEN_OPTS = '-Xmx4g'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '25'))
        skipDefaultCheckout()
        timeout(time: 2, unit: 'HOURS')
    }

    parameters {
        booleanParam defaultValue: false,
                description: 'Skip test and sonar analysis.',
                name: 'SKIP_TEST_WITH_COVERAGE'
        booleanParam defaultValue: false,
                description: 'Skip sonar analysis.',
                name: 'SKIP_SONARQUBE'
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
                                client common packages soap jython-libs \
                                staging/
                    """
                            stash includes: 'staging/**', name: 'staging'
                        }
                    }
                }
                stage('Build containers') {
                    steps {
                        container('dind') {
                            withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
                                sh 'docker buildx bake --no-cache'
                            }
                        }
                    }
                }
            }
        }

        stage('UT, IT') {
            steps {
                container('jdk-21') {
                    sh "mvn ${MVN_OPTS} verify -DexcludedGroups=api,flaky,e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }
        stage('Flaky, API, E2E tests') {
            steps {
                container('jdk-21') {
                    sh "cd store && mvn ${MVN_OPTS} verify -Dgroups=flaky,api && mvn ${MVN_OPTS} verify -Dgroups=e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }

        stage('Generate SOAP API Docs') {
            steps {
                container('jdk-21') {
                    sh """
                        cd soap
                        mvn ${MVN_OPTS} antrun:run@generate-soap-docs
                        cd ..
                    """
                }
            }
        }

        stage('Package API Docs') {
            steps {
                container('jdk-21') {
                    sh """
                        VERSION=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                        mkdir -p api-docs
                        if [ -d "soap/target/docs/soap" ]; then
                            cp -r soap/target/docs/soap api-docs/
                        fi
                        if [ -d "soap/target/docs/soapapi-changelog" ]; then
                            cp -r soap/target/docs/soapapi-changelog api-docs/
                        fi
                        if [ -f "soap/target/docs/soapapi-doc.zip" ]; then
                            cp soap/target/docs/soapapi-doc.zip api-docs/
                        fi
                        if [ -f "soap/target/docs/soapapi-changelog.zip" ]; then
                            cp soap/target/docs/soapapi-changelog.zip api-docs/
                        fi
                        tar -czf carbonio-mailbox-api-docs-\${VERSION}.tar.gz api-docs/
                    """
                }
                archiveArtifacts artifacts: 'carbonio-mailbox-api-docs-*.tar.gz', allowEmptyArchive: true
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

        stage('Publish SNAPSHOT to maven') {
            when {
                not { buildingTag() }
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

        stage('Publish docker images') {
            steps {
                dockerStage([
                        dockerfile: 'docker/mailbox/Dockerfile',
                        imageName : 'carbonio-mailbox',
                        ocLabels  : [
                            title          : 'Carbonio Mailbox',
                            descriptionFile: 'docker/mailbox/description.md'
                        ]
                ])
                dockerStage([
                        dockerfile: 'docker/mariadb/Dockerfile',
                        imageName : 'carbonio-mariadb',
                        ocLabels  : [
                                title          : 'Carbonio MariaDB',
                                descriptionFile: 'docker/mariadb/description.md'
                        ]
                ])
            }
        }

        stage('Build deb/rpm') {
            steps {
                echo 'Building deb/rpm packages'
                buildStage([
                        skipStash: true,
                        buildDirs: ['staging/packages'],
                        overrides: [
                                ubuntu: [
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
