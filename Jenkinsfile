library(
        identifier: 'jenkins-lib-common@maven-refactor',
        retriever: modernSCM([
                $class: 'GitSCMSource',
                credentialsId: 'jenkins-integration-with-github-account',
                remote: 'git@github.com:zextras/jenkins-lib-common.git',
        ])
)

String profile = env.TAG_NAME ? '-Pprod' :
        (env.BRANCH_NAME == 'devel' ? '-Pdev' : '')
String mvnOpts = "-Ddebug=0 -Dis-production=1 ${profile}"

defaultPipeline(timeoutMin: 120) {
    withEnv(['MAVEN_OPTS=-Xmx2g']) {

        stage('Build') {
            withMaven {
                sh """
                    mvn ${mvnOpts} -s \$SETTINGS_PATH \
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

        stage('UT, IT') {
            withMaven {
                sh "mvn ${mvnOpts} -s \$SETTINGS_PATH jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -DexcludedGroups=api,flaky,e2e"
            }
            junit allowEmptyResults: true,
                    testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
        }

        stage('Flaky, API, E2E tests') {
            withMaven {
                sh "cd store && mvn ${mvnOpts} -s \$SETTINGS_PATH jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=flaky,api && mvn ${mvnOpts} -s \$SETTINGS_PATH jacoco:prepare-agent surefire:test failsafe:integration-test failsafe:verify -Dgroups=e2e"
            }
            junit allowEmptyResults: true,
                    testResults: '**/target/surefire-reports/*.xml,**/target/failsafe-reports/*.xml'
        }

        stage('Build and Package API Docs') {
            withMaven {
                sh """
                    (
                        cd soap || { echo "Directory soap does not exist"; exit 1; }
                        mvn ${mvnOpts} -s \$SETTINGS_PATH antrun:run@generate-soap-docs
                    )
                    VERSION=\$(mvn help:evaluate -s \$SETTINGS_PATH -Dexpression=project.version -q -DforceStdout)
                    mkdir -p docs
                    tar -czf docs/carbonio-mailbox-api-docs-\${VERSION}.tar.gz -C soap/target/docs/soap .
                """
            }
            archiveArtifacts artifacts: 'docs/carbonio-mailbox-api-docs-*.tar.gz', allowEmptyArchive: true
        }

        stage('Sonarqube Analysis') {
            withMaven {
                withSonarQube {
                    sh """
                        mvn ${mvnOpts} -s \$SETTINGS_PATH \
                            jacoco:report \
                            sonar:sonar \
                            -Dsonar.coverage.jacoco.xmlReportPaths=**/target/site/jacoco/jacoco.xml \
                            -Dsonar.junit.reportPaths=target/surefire-reports,target/failsafe-reports \
                            -Dsonar.exclusions=**/com/zimbra/soap/mail/type/*.java,**/com/zimbra/soap/mail/message/*.java,**/com/zimbra/cs/account/ZAttr*.java,**/com/zimbra/common/account/ZAttr*.java
                    """
                }
            }
        }

        stage('Build and upload artifacts') {
            parallel(
                    'Packages': {
                        stage('Build deb/rpm') {
                            echo 'Building deb/rpm packages'
                            buildStage([
                                    addCarbonioRepos: true,
                                    carbonioRepoCredentialId: 'artifactory-jenkins-gradle-properties-splitted',
                                    skipStash: true,
                                    buildDirs: ['staging/packages'],
                            ])
                        }
                        stage('Publish packages') {
                            def jfrogHome = tool name: 'jfrog-cli'
                            withEnv(["PATH+JFROG=${jfrogHome}/bin"]) {
                                uploadStage(
                                        packages: yapHelper.getPackageNames('staging/packages/yap.json')
                                )
                            }
                        }
                    },

                    'Publish SNAPSHOT to maven': {
                        if (!env.TAG_NAME && env.BRANCH_NAME == 'devel') {
                            stage('Publish SNAPSHOT to maven') {
                                withMaven {
                                    sh "mvn ${mvnOpts} -s \$SETTINGS_PATH deploy -DskipTests=true"
                                }
                            }
                        }
                    },

                    'Publish to maven': {
                        if (env.TAG_NAME) {
                            stage('Publish to maven') {
                                withMaven {
                                    sh "mvn ${mvnOpts} -s \$SETTINGS_PATH deploy -Dchangelist= -DskipTests=true"
                                }
                            }
                        }
                    },

                    'Docker images': {
                        stage('Build and Publish Docker images') {
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
                                            title: 'Carbonio Mailbox Sidecar',
                                    ]
                            ])
                            dockerStage([
                                    dockerfile: 'docker/mailbox-admin-sidecar/Dockerfile',
                                    imageName : 'carbonio-mailbox-admin-sidecar',
                                    platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                    ocLabels  : [
                                            title: 'Carbonio Mailbox Admin Sidecar',
                                    ]
                            ])
                            dockerStage([
                                    dockerfile: 'docker/mailbox-nslookup-sidecar/Dockerfile',
                                    imageName : 'carbonio-mailbox-nslookup-sidecar',
                                    platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                    ocLabels  : [
                                            title: 'Carbonio Mailbox NSLookup Sidecar',
                                    ]
                            ])
                            dockerStage([
                                    dockerfile: 'docker/mailbox-internal-api-sidecar/Dockerfile',
                                    imageName : 'carbonio-mailbox-internal-api-sidecar',
                                    platforms : ['linux/amd64', 'linux/arm64'] as Set,
                                    ocLabels  : [
                                            title: 'Carbonio Mailbox Internal API Sidecar',
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
                    },
            )
        }
    }
}