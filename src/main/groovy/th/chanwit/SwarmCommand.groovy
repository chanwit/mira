package th.chanwit

import de.gesellix.docker.client.DockerClient

class SwarmCommand {

    private DockerClient dockerClient

    SwarmCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    class JoinTokenAction {

        String getManager() {
            def c = dockerClient.inspectSwarm().content
            return c.JoinTokens.Manager
        }

        String getWorker() {
            def c = dockerClient.inspectSwarm().content
            return c.JoinTokens.Worker
        }

    }

    def join(Symbol action) {
        println ">> $action"
        if ("$action" == "-token") {
            return new JoinTokenAction()
        }
        throw new Exception("NYI")
    }

    def join(Map map, Symbol arg) {

    }

}

