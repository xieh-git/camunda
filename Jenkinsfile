#!/usr/bin/env groovy

// https://github.com/jenkinsci/pipeline-model-definition-plugin/wiki/Getting-Started

// general properties for CI execution
def static NODE_POOL() { return "slaves" }
def static MAVEN_DOCKER_IMAGE() { return "maven:3.5.3-jdk-8-slim" }
def static NODEJS_DOCKER_IMAGE() { return "node:8.11.2-alpine" }
def static DIND_DOCKER_IMAGE() { return "docker:18.03.1-ce-dind" }
def static PROJECT_DOCKER_IMAGE() { return "gcr.io/ci-30-162810/camunda-optimize" }

String getGitCommitMsg() {
  return sh(script: 'git log --format=%B -n 1 HEAD', returnStdout: true).trim()
}

String getGitCommitHash() {
  return sh(script: 'git rev-parse --verify HEAD', returnStdout: true).trim()
}

void buildNotification(String buildStatus) {
  // build status of null means successful
  buildStatus = buildStatus ?: 'SUCCESS'

  String buildResultUrl = "${env.BUILD_URL}"
  if(env.RUN_DISPLAY_URL) {
    buildResultUrl = "${env.RUN_DISPLAY_URL}"
  }

  def subject = "[${buildStatus}] - ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}"
  def body = "See: ${buildResultUrl}"
  def recipients = [[$class: 'CulpritsRecipientProvider'], [$class: 'RequesterRecipientProvider']]

  emailext subject: subject, body: body, recipientProviders: recipients
}

static String mavenNodeJSDindAgent(env) {
  return """
apiVersion: v1
kind: Pod
metadata:
  labels:
    agent: optimize-ci-build
spec:
  nodeSelector:
    cloud.google.com/gke-nodepool: ${NODE_POOL()}
  containers:
  - name: maven
    image: ${MAVEN_DOCKER_IMAGE()}
    command: ["cat"]
    tty: true
    env:
      # every JVM process will get a 1/4 of HEAP from total memory
      - name: JAVA_TOOL_OPTIONS
        value: |
          -XX:+UnlockExperimentalVMOptions
          -XX:+UseCGroupMemoryLimitForHeap
      - name: LIMITS_CPU
        valueFrom:
          resourceFieldRef:
            resource: limits.cpu
      - name: TZ
        value: Europe/Berlin
      - name: DOCKER_HOST
        value: tcp://localhost:2375
    resources:
      limits:
        cpu: 2
        memory: 2Gi
      requests:
        cpu: 2
        memory: 2Gi
  - name: node
    image: ${NODEJS_DOCKER_IMAGE()}
    command: ["cat"]
    tty: true
    env:
      - name: LIMITS_CPU
        valueFrom:
          resourceFieldRef:
            resource: limits.cpu
    resources:
      limits:
        cpu: 2
        memory: 1Gi
      requests:
        cpu: 2
        memory: 1Gi
  - name: docker
    image: ${DIND_DOCKER_IMAGE()}
    args: ["--storage-driver=overlay2"]
    securityContext:
      privileged: true
    tty: true
    resources:
      limits:
        cpu: 4
        memory: 4Gi
      requests:
        cpu: 4
        memory: 4Gi
"""
}

void integrationTestSteps(String engineVersion = 'latest') {
  container('node') {
    sh ('''
      cd ./client
      ./build_client.sh $(pwd)
    ''')
  }
  container('maven') {
    installDockerCompose()
    setupPermissionsForHostDirs('backend')
    runMaven("-T\$LIMITS_CPU -Pproduction,it,engine-${engineVersion},postgresql -pl backend -am install")
  }
}

void setupPermissionsForHostDirs(String directory) {
  sh("""#!/bin/bash -ex
    mkdir -p ${directory}/target/{es_logs,es_snapshots,cambpm_logs}
    # must be 1000 so ES and CamBPM can write to the mounted volumes defined in docker-compose.yml
    chown -R 1000:1000 ${directory}/target/{es_logs,es_snapshots,cambpm_logs}
  """)
}

void archiveTestArtifacts(String directory) {
  container('maven') {
    // fix permissions for jnlp slave as maven user is root so we can archive the artifacts
    sh """#!/bin/bash -ex
      chown -R 10000:10000 ${directory}/target/{es_logs,es_snapshots,cambpm_logs}
    """
  }

  archiveArtifacts(
    artifacts: "${directory}/target/es_*/**/*,${directory}/target/cambpm_logs/**/*,**/target/*-reports/**/*.txt",
    allowEmptyResults: true,
    onlyIfSuccessful: false
  )
}

void installDockerCompose(version = '1.21.2') {
  sh("""
    curl -sSL https://github.com/docker/compose/releases/download/${version}/docker-compose-Linux-x86_64 -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    docker-compose version
  """)
}

void runMaven(String cmd) {
  sh ("mvn ${cmd} -s settings.xml -B --fail-at-end -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn")
}

/******** START PIPELINE *******/

pipeline {
  agent {
    kubernetes {
      cloud 'optimize-ci'
      label "optimize-ci-build_${env.JOB_BASE_NAME}-${env.BUILD_ID}"
      defaultContainer 'jnlp'
      yaml mavenNodeJSDindAgent(env)
    }
  }

  // Environment
  environment {
    NODE_ENV = "ci"
    NEXUS = credentials("camunda-nexus")
  }

  options {
    buildDiscarder(logRotator(numToKeepStr:'50', artifactNumToKeepStr: '3'))
    timestamps()
    timeout(time: 30, unit: 'MINUTES')
  }

  stages {
    stage('Unit tests') {
      parallel {
        stage('Backend') {
          steps {
            container('maven') {
              installDockerCompose()
              setupPermissionsForHostDirs('upgrade')
              runMaven('-Dskip.fe.build=true -T\$LIMITS_CPU install')
            }
          }
          post {
            always {
              junit testResults: '**/surefire-reports/**/*.xml', keepLongStdio: true
              archiveTestArtifacts('upgrade')
            }
          }
        }
        stage('Frontend') {
          steps {
            container('node') {
              sh ('''
                cd ./client
                ./build_client.sh $(pwd)
                yarn test:ci
              ''')
            }
          }
          post {
            always {
              junit testResults: 'client/jest-test-results.xml', keepLongStdio: true, allowEmptyResults: true
            }
          }
        }
      }
    }
    stage('ITs against different engines') {
      parallel {
        stage('IT Latest') {
          agent {
            kubernetes {
              cloud 'optimize-ci'
              label "optimize-ci-build-it-latest_${env.JOB_BASE_NAME}-${env.BUILD_ID}"
              defaultContainer 'jnlp'
              yaml mavenNodeJSDindAgent(env)
            }
          }
          steps {
            integrationTestSteps('latest')
          }
          post {
            always {
              junit testResults: 'backend/target/failsafe-reports/**/*.xml', allowEmptyResults: true, keepLongStdio: true
              archiveTestArtifacts('backend')
            }
          }
        }
//        stage('IT 7.9') {
//          when {
//            beforeAgent true
//            branch 'master'
//          }
//          agent {
//            kubernetes {
//              cloud 'optimize-ci'
//              label "optimize-ci-build-it-7.9_${env.JOB_BASE_NAME}-${env.BUILD_ID}"
//              defaultContainer 'jnlp'
//              yaml mavenNodeJSDindAgent(env)
//            }
//          }
//          steps {
//            integrationTestSteps('7.9')
//          }
//          post {
//            always {
//              junit testResults: 'backend/target/failsafe-reports/**/*.xml', allowEmptyResults: true, keepLongStdio: true
//              archiveTestArtifacts('backend')
//            }
//          }
//        }
//        stage('IT 7.8') {
//          when {
//            beforeAgent true
//            branch 'master'
//          }
//          agent {
//            kubernetes {
//              cloud 'optimize-ci'
//              label "optimize-ci-build-it-7.8_${env.JOB_BASE_NAME}-${env.BUILD_ID}"
//              defaultContainer 'jnlp'
//              yaml mavenNodeJSDindAgent(env)
//            }
//          }
//          steps {
//            integrationTestSteps('7.8')
//          }
//          post {
//            always {
//              junit testResults: 'backend/target/failsafe-reports/**/*.xml', allowEmptyResults: true, keepLongStdio: true
//              archiveTestArtifacts('backend')
//            }
//          }
//        }
      }
    }
    stage('RESTAPI Docs') {
      steps {
        container('maven') {
          runMaven('-f backend/pom.xml -DskipTests -Pdocs clean package')
        }
      }
      post {
        success {
          archiveArtifacts artifacts: 'backend/target/docs/**/*.*'
        }
      }
    }
    stage('Deploy to Nexus') {
      when {
        branch 'master'
      }
      steps {
        container('maven') {
          runMaven('-Pproduction -Dskip.fe.build -DskipTests deploy')
        }
      }
    }
    stage('Build Docker') {
      when {
        branch 'master'
      }
      environment {
        VERSION = readMavenPom().getVersion().replace('-SNAPSHOT', '')
        SNAPSHOT = readMavenPom().getVersion().contains('SNAPSHOT')
        COMMIT_ID = getGitCommitHash()
        REGISTRY = credentials('docker-registry-ci3')
      }
      steps {
        container('docker') {
          sh """
            echo '${REGISTRY}' | docker login -u _json_key https://gcr.io --password-stdin
            
            docker build -t ${PROJECT_DOCKER_IMAGE()}:${COMMIT_ID} \
              --build-arg=VERSION=${VERSION} \
              --build-arg=SNAPSHOT=${SNAPSHOT} \
              --build-arg=USERNAME=${NEXUS_USR} \
              --build-arg=PASSWORD=${NEXUS_PSW} \
              .

            docker push ${PROJECT_DOCKER_IMAGE()}:${COMMIT_ID}
          """
        }
      }
    }
  }

  post {
    changed {
      buildNotification(currentBuild.result)
    }
  }
}
