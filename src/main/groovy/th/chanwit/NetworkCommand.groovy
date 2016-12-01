package th.chanwit

import de.gesellix.docker.client.DockerClient
import th.chanwit.plugin.Interceptor

class NetworkCommand {

    private DockerClient dockerClient

    NetworkCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    def create(Map map, Symbol arg) {

        Interceptor interceptor = BaseScript.interceptor.get()
        if (interceptor) {
            (map, arg) = interceptor.beforeNetworkCreate(map, arg)
        }

        if (!map['driver']) map['driver'] = "bridge"

        def networkConfig = [
                "Driver"        : "${map['driver']}",
                "EnableIPv6"    : false,
                "IPAM"          : [
                        "Driver" : "default",
                        "Options": null,
                        "Config" : [],
                ],
                "CheckDuplicate": true,
        ]

        def id = dockerClient.createNetwork("$arg", networkConfig).content.Id
        println "${id[0..11]} network '$arg' created"
        return id
    }

    def rm(Symbol arg) {

        Interceptor interceptor = BaseScript.interceptor.get()
        if (interceptor) {
            arg = interceptor.beforeNetworkRm(arg)
        }

        def result = dockerClient.rmNetwork("$arg")
        println "$arg network removed"
        return result
    }

}