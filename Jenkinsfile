def odsNamespace
def odsGitRef
def odsImageTag
node {
  odsNamespace = env.ODS_NAMESPACE ?: 'ods'
  odsImageTag = env.ODS_IMAGE_TAG ?: 'latest'
  odsGitRef = env.ODS_GIT_REF ?: 'master'
}

library("ods-jenkins-shared-library@${odsGitRef}")

odsComponentPipeline(
  imageStreamTag: "${odsNamespace}/jenkins-slave-maven:${odsImageTag}",
  branchToEnvironmentMapping: [
    '*': 'dev',
    "${odsGitRef}" : 'test'
  ]
) { context ->
  stageBuild(context)
  odsComponentStageScanWithSonar(context, [branch: '*'])
  odsComponentStageBuildOpenShiftImage(context)
}

def stageBuild(def context) {
  def javaOpts = "-Xmx512m"
  def gradleTestOpts = "-Xmx128m"

  stage('Build and Unit Test') {
    withEnv(["TAGVERSION=${context.tagversion}", "NEXUS_HOST=${context.nexusHost}", "NEXUS_USERNAME=${context.nexusUsername}", "NEXUS_PASSWORD=${context.nexusPassword}", "JAVA_OPTS=${javaOpts}", "GRADLE_TEST_OPTS=${gradleTestOpts}"]) {

      // get wkhtml
      sh (
        script : """
        curl -kLO https://downloads.wkhtmltopdf.org/0.12/0.12.4/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz
        tar vxf wkhtmltox-0.12.4_linux-generic-amd64.tar.xz
        mv wkhtmltox/bin/wkhtmlto* /usr/bin
        """,
        label : "get and install wkhtml"
      )

      def status = sh(
        script: "./gradlew clean test shadowJar --stacktrace --no-daemon",
        returnStatus: true
      )
      if (status != 0) {
        error "Build failed!"
      }

      status = sh(
        script: "cp build/libs/*-all.jar ./docker/app.jar",
        returnStatus: true
      )
      if (status != 0) {
        error "Copying failed!"
      }
    }
  }
}
