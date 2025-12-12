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
                        container('jdk-17') {
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
                container('jdk-17') {
                    sh "mvn ${MVN_OPTS} verify -DexcludedGroups=api,flaky,e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }
        stage('Flaky, API, E2E tests') {
            steps {
                container('jdk-17') {
                    sh "cd store && mvn ${MVN_OPTS} verify -Dgroups=flaky,api && mvn ${MVN_OPTS} verify -Dgroups=e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
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

        stage('Publish to maven') {
            when {
                expression {
                    return isBuildingTag() || env.BRANCH_NAME == 'devel'
                }
            }
            steps {
                container('jdk-17') {
                    withCredentials([file(credentialsId: 'jenkins-maven-settings.xml', variable: 'SETTINGS_PATH')]) {
                        script {
                            sh "mvn ${MVN_OPTS} -s " + SETTINGS_PATH + " deploy -DskipTests=true"
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
                            descriptionFile: 'docker/mailbox/description.md',
                            version        : "${isBuildingTag() ? env.TAG_NAME : 'devel'}"
                        ]
                ])
                dockerStage([
                        dockerfile: 'docker/mariadb/Dockerfile',
                        imageName : 'carbonio-mariadb',
                        ocLabels  : [
                                title          : 'Carbonio MariaDB',
                                descriptionFile: 'docker/mariadb/description.md',
                                version        : "${isBuildingTag() ? env.TAG_NAME : 'devel'}"
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
