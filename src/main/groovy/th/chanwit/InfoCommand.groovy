package th.chanwit

import de.gesellix.docker.client.DockerClient

class InfoCommand {

    private DockerClient dockerClient

    InfoCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    def info() {
        return dockerClient.info().content
    }

}
