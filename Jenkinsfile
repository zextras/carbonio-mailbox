library(
        identifier: 'jenkins-packages-build-library@1.0.4',
        retriever: modernSCM([
                $class       : 'GitSCMSource',
                remote       : 'git@github.com:zextras/jenkins-packages-build-library.git',
                credentialsId: 'jenkins-integration-with-github-account'
        ])
)

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
        stage('Checkout') {
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
                        cp -a store* milter* attribute-manager right-manager \
                                mailbox-ldap-lib client common packages soap jython-libs \
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
                                sh 'docker buildx bake'
                            }
                        }
                    }
                }
            }
        }


        stage('UT, IT') {
            steps {
                container('jdk-17') {
                    sh "mvn ${MVN_OPTS} verify -DexcludedGroups=api,special"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }
        stage('API tests') {
            steps {
                container('jdk-17') {
                    sh "mvn ${MVN_OPTS} verify -Dgroups=api"
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
                            mvn ${MVN_OPTS} -DskipTests \
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
                            Set<String> tagVersions = []
                            if (isBuildingTag()) {
                                tagVersions = [env.TAG_NAME, 'stable']
                            } else {
                                tagVersions = ['devel', 'latest']
                            }
                            dockerHelper.buildImage([
                                    dockerfile: 'docker/standalone/mailbox/Dockerfile',
                                    imageName : 'registry.dev.zextras.com/dev/carbonio-mailbox',
                                    imageTags : tagVersions,
                                    ocLabels  : [
                                            title          : 'Carbonio Mailbox',
                                            descriptionFile: 'docker/standalone/mailbox/description.md',
                                            version        : tagVersions[0]
                                    ]
                            ])
                            dockerHelper.buildImage([
                                    dockerfile: 'docker/standalone/mariadb/Dockerfile',
                                    imageName : 'registry.dev.zextras.com/dev/carbonio-mariadb',
                                    imageTags : tagVersions,
                                    ocLabels  : [
                                            title          : 'Carbonio MariaDB',
                                            descriptionFile: 'docker/standalone/mariadb/description.md',
                                            version        : tagVersions[0]
                                    ]
                            ])
                            dockerHelper.buildImage([
                                    dockerfile: 'docker/standalone/openldap/Dockerfile',
                                    imageName : 'registry.dev.zextras.com/dev/carbonio-openldap',
                                    imageTags : tagVersions,
                                    ocLabels  : [
                                            title          : 'Carbonio OpenLDAP',
                                            descriptionFile: 'docker/standalone/openldap/description.md',
                                            version        : tagVersions[0]
                                    ]
                            ])
                        }
                    }
                }
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
                    steps {
                        uploadStage(
                                packages: yapHelper.getPackageNames('staging/packages/yap.json')
                        )
                    }
                }
    }
}
