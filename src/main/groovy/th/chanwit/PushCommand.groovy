package th.chanwit

import de.gesellix.docker.client.DockerClient

class PushCommand {

    private DockerClient dockerClient

    PushCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    def propertyMissing(String name) {
        push(name)
    }

    def push(image) {
        def authDetails = dockerClient.readAuthConfig(null, null)
        def authBase64Encoded = dockerClient.encodeAuthConfig(authDetails)
        def result = dockerClient.push("$image", authBase64Encoded)
        println "${result.content.last().aux.Digest[7..18]} pushed '$image' successfully"
        return MiraAction.error
    }

}