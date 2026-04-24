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
                                client common packages soap jython-libs \
                                staging/
                    """
                            stash includes: 'staging/**', name: 'staging'
                        }
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
                    }
                }
            }

        }

    }
}
