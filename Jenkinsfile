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
        booleanParam defaultValue: true,
                description: 'Skip test and sonar analysis.',
                name: 'SKIP_TEST_WITH_COVERAGE'
        booleanParam defaultValue: true,
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
