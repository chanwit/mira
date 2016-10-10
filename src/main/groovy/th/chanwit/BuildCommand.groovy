package th.chanwit

import de.gesellix.docker.client.*
import de.gesellix.docker.client.builder.BuildContextBuilder

class BuildCommand {

  private DockerClient dockerClient

  InputStream newBuildContext(File baseDirectory) {
    def buildContext = File.createTempFile("buildContext", ".tar")
    buildContext.deleteOnExit()
    BuildContextBuilder.archiveTarFilesRecursively(baseDirectory, buildContext)
    return new FileInputStream(buildContext)
  }

  BuildCommand(DockerClient cli){
    this.dockerClient = cli
  }

  def build(Map map, Symbol arg) {
    def dir = new File("$arg")
    if (dir.exists() == false) {
      println "$arg skipped the build"
      return MiraAction.error
    }

    def buildResult = dockerClient.build(newBuildContext(dir))
    if (buildResult =~ /\w{12}/) {
      if (map["tag"]) {
        def tagResult = dockerClient.tag("$buildResult", map["tag"])
        if (tagResult.status.code == 201) {
          println "${buildResult} ${map['tag']} built successfully"
          return MiraAction.build
        } else {
          return MiraAction.error
        }
      }
      return MiraAction.build
    }
    return MiraAction.error
  }

}