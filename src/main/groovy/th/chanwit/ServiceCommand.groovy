package th.chanwit

import de.gesellix.docker.client.DockerClient
import th.chanwit.plugin.Interceptor

class ServiceCommand {

    private DockerClient dockerClient

    ServiceCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    def create(Map map, String args = "") {

        Interceptor interceptor = BaseScript.interceptor.get()
        if (interceptor) {
            (map, args) = interceptor.beforeServiceCreate(map, args)
        }

        if (!map["image"]) throw new Exception("service create: no 'image' specified")

        def publishes = []
        if (map["publish"]) {
            publishes = map["publish"].collect { k, v ->
                def protocol = "tcp"
                if ("$v".contains("/")) {
                    def parts = "$v".split("/")
                    v = parts[0]
                    protocol = parts[1]
                }

                return [
                        "Protocol"     : protocol,
                        "PublishedPort": Integer.valueOf(k),
                        "TargetPort"   : Integer.valueOf(v)
                ]
            }
        }

        def networks = [:]
        if (map['network']) {
            // convert Symbol to string
            networks = map['network'].collect { [Target: "$it"] }
        }

        def replicas = 1
        if (map["replicas"]) {
            replicas = map["replicas"]
        }
        if (replicas < 1) throw new Exception("service create: replicas cannot be < 1")

        def serviceConfig = [
                "Name"        : "${map['imageName']}",
                "TaskTemplate": [
                        "ContainerSpec": [
                                "Image": "${map['image']}",
                        ],
                        "Resources"    : [
                                "Limits"      : [:],
                                "Reservations": [:],
                        ],
                        "RestartPolicy": [:],
                        "Placement"    : [:],
                ],
                "Mode"        : [
                        "Replicated": [
                                "Replicas": replicas,
                        ]
                ],
                "UpdateConfig": [
                        "Parallelism": 1
                ],
                "EndpointSpec": [
                        "Ports": publishes,
                ],
                "Networks"    : networks,
        ]
        // println serviceConfig
        def id = dockerClient.createService(serviceConfig).content.ID
        println "${id[0..11]} service '${map['imageName']}' created from ${map['image']}"
        return id
    }

    def rm(name) {

        Interceptor interceptor = BaseScript.interceptor.get()
        if (interceptor) {
            name = interceptor.beforeServiceRm("$name")
        }

        try {
            def result = dockerClient.rmService("$name")
            println "$name service removed"
        } catch (e) {
            println "failed to remove $name"
        }
    }

    def ls(args) {
        dockerClient.services(args).content
    }

    def getLs() {
        dockerClient.services().content
    }

}
