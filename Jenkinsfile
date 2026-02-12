library(
        identifier: 'jenkins-lib-common@feat/add-maven',
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

pipeline {
    agent {
        node {
            label 'zextras-v1'
        }
    }

    environment {
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

        stage('Build containers') {
            steps {
                container('dind') {
                    withDockerRegistry(credentialsId: 'private-registry', url: 'https://registry.dev.zextras.com') {
                        sh 'docker buildx bake --no-cache'
                    }
                }
            }
        }

        stage('Maven') {
            steps {
                script {
                    mavenStage(
                            container: 'jdk-21',
                            buildGoals: 'clean install',
                            postBuildScript: '''\
                                mkdir staging
                                cp -a store* right-manager \
                                        client common packages soap jython-libs \
                                        staging/
                            '''.stripIndent().trim(),
                            testOpts: [DexcludedGroups: 'api,flaky,e2e'],
                            skipTests: params.SKIP_TEST_WITH_COVERAGE,
                            skipCoverage: params.SKIP_TEST_WITH_COVERAGE,
                            skipSonar: params.SKIP_SONARQUBE || params.SKIP_TEST_WITH_COVERAGE,
                            mvnOpts: [Ddebug: '0', 'Dis-production': '1']
                    )
                }
                stash includes: 'staging/**', name: 'staging'
            }
        }

        stage('Flaky, API, E2E tests') {
            steps {
                container('jdk-21') {
                    sh "cd store && mvn verify -B -Dgroups=flaky,api && mvn verify -B -Dgroups=e2e"
                }
                junit allowEmptyResults: true,
                        testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
            }
        }

        stage('Build and Package API Docs') {
            steps {
                container('jdk-21') {
                    sh """
                        cd soap
                        mvn ${MVN_OPTS} antrun:run@generate-soap-docs
                        cd ..
                        VERSION=\$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
                        mkdir -p artifacts
                        tar -czf artifacts/carbonio-mailbox-api-docs-\${VERSION}.tar.gz -C soap/target/docs/soap .
                    """
                }
                archiveArtifacts artifacts: 'artifacts/carbonio-mailbox-api-docs-*.tar.gz', allowEmptyArchive: true
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
