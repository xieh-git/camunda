#!/usr/bin/env groovy

// https://github.com/camunda/jenkins-global-shared-library
@Library(["camunda-ci", "optimize-jenkins-shared-library"]) _

// general properties for CI execution
def static NODE_POOL() { return "agents-n1-standard-32-netssd-stable" }

def static MAVEN_DOCKER_IMAGE() { return "maven:3.8.1-jdk-11-slim" }

def static CAMBPM_DOCKER_IMAGE(String camBpmVersion) {
  return "registry.camunda.cloud/cambpm-ee/camunda-bpm-platform-ee:${camBpmVersion}"
}

static String mavenElasticsearchAWSIntegrationTestAgent(camBpmVersion) {
  return itStageBasePod() + camBpmContainerSpec(camBpmVersion)
}

static String itStageBasePod() {
  return """
metadata:
  labels:
    agent: optimize-ci-build
spec:
  nodeSelector:
    cloud.google.com/gke-nodepool: ${NODE_POOL()}
  tolerations:
    - key: "${NODE_POOL()}"
      operator: "Exists"
      effect: "NoSchedule"
  imagePullSecrets:
    - name: registry-camunda-cloud
  volumes:
  - name: cambpm-config
    configMap:
      # Defined in: https://github.com/camunda/infra-core/tree/master/camunda-ci/deployments/optimize
      name: ci-optimize-cambpm-config
  initContainers:
    - name: init-sysctl
      image: busybox
      imagePullPolicy: Always
      command: ["sysctl", "-w", "vm.max_map_count=262144"]
      securityContext:
        privileged: true
  containers:
  - name: maven
    image: ${MAVEN_DOCKER_IMAGE()}
    command: ["cat"]
    tty: true
    env:
      - name: LIMITS_CPU
        value: 2
      - name: TZ
        value: Europe/Berlin
    resources:
      limits:
        cpu: 4
        memory: 6Gi
      requests:
        cpu: 4
        memory: 6Gi
"""
}

static String camBpmContainerSpec(String camBpmVersion) {
  return """
  - name: cambpm
    image: ${CAMBPM_DOCKER_IMAGE(camBpmVersion)}
    imagePullPolicy: Always
    env:
      - name: JAVA_OPTS
        value: "-Xms1g -Xmx1g -XX:MaxMetaspaceSize=256m"
      - name: TZ
        value: Europe/Berlin
    resources:
      limits:
        cpu: 2
        memory: 2Gi
      requests:
        cpu: 2
        memory: 2Gi
    volumeMounts:
    - name: cambpm-config
      mountPath: /camunda/conf/tomcat-users.xml
      subPath: tomcat-users.xml
    - name: cambpm-config
      mountPath: /camunda/webapps/manager/META-INF/context.xml
      subPath: context.xml
    """
}

void runMaven(String cmd) {
  configFileProvider([configFile(fileId: 'maven-nexus-settings-local-repo', variable: 'MAVEN_SETTINGS_XML')]) {
    sh("mvn ${cmd} -s \$MAVEN_SETTINGS_XML -B --fail-at-end -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn")
  }
}

void integrationTestStepsAWS() {
  optimizeCloneGitRepo(params.BRANCH)
  container('maven') {
    sh("""    
      curl -s "http://$OPTIMIZE_ELASTICSEARCH_HOST/_cat/indices?v"
      
      #cleanup before starting the integration tests to assure starting from scratch
      curl -XDELETE "http://$OPTIMIZE_ELASTICSEARCH_HOST/_all"
      """)
    runMaven("verify -Dskip.docker -Pit,engine-latest -pl backend,upgrade -am -T\$LIMITS_CPU -DhttpTestTimeout=60000")
  }
}


/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. Licensed under a commercial license.
 * You may not use this file except in compliance with the commercial license.
 */
pipeline {
  agent none
  environment {
    NEXUS = credentials("camunda-nexus")
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
    timestamps()
    timeout(time: 480, unit: 'MINUTES')
  }

  stages {
    stage("Prepare") {
      agent {
        kubernetes {
          cloud 'optimize-ci'
          label "optimize-ci-build-${env.JOB_BASE_NAME}-${env.BUILD_ID}"
          defaultContainer 'jnlp'
          yaml plainMavenAgent(NODE_POOL(), MAVEN_DOCKER_IMAGE())
        }
      }
      steps {
        optimizeCloneGitRepo(params.BRANCH)
        setBuildEnvVars()
      }
    }
    stage("Elasticsearch AWS Integration") {
      agent {
        kubernetes {
          cloud 'optimize-ci'
          label "optimize-ci-build_es-AWS_${env.JOB_BASE_NAME}-${env.BUILD_ID}"
          defaultContainer 'jnlp'
          yaml mavenElasticsearchAWSIntegrationTestAgent("${env.CAMBPM_VERSION}")
        }
      }

      environment {
        OPTIMIZE_ELASTICSEARCH_HOST = "ci-elasticsearch.optimize"
        OPTIMIZE_ELASTICSEARCH_HTTP_PORT = 80
      }

      steps {
        integrationTestStepsAWS()
      }
      post {
        always {
          junit testResults: 'backend/target/failsafe-reports/**/*.xml', allowEmptyResults: true, keepLongStdio: true
        }
      }
    }
  }

  post {
    changed {
      sendEmailNotification()
    }
    always {
      retriggerBuildIfDisconnected()
    }
  }
}
