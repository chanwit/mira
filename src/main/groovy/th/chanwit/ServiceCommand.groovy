package th.chanwit

import de.gesellix.docker.client.*

class ServiceCommand {

  private DockerClient dockerClient

  ServiceCommand(DockerClient cli){
    this.dockerClient = cli
  }

  def create(Map map, String args="") {
    if (!map["image"]) throw new Exception("service create: no 'image' specified")

    def publish = []
    if(map["publish"]) {
      publish = map["publish"].collect { k, v ->
        def protocol = "tcp"
        if("$v".contains("/")) {
          def parts = "$v".split("/")
          v = parts[0]
          protocol = parts[1]
        }

        return [
          "Protocol": protocol,
          "PublishedPort": Integer.valueOf(k),
          "TargetPort": Integer.valueOf(v)
        ]
      }
    }

    def replicas = 1
    if(map["replicas"]) {
      replicas = map["replicas"]
    }
    if (replicas < 1) throw new Exception("service create: replicas cannot be < 1")

    def serviceConfig = [
      "Name": "${map['name']}",
      "TaskTemplate": [
      "ContainerSpec": [
        "Image": "${map['image']}",
      ],
      "Resources": [
        "Limits": [:],
        "Reservations": [:],
      ],
      "RestartPolicy": [:],
        "Placement": [:],
      ],
      "Mode": [
        "Replicated": [
          "Replicas": replicas,
        ]
      ],
      "UpdateConfig": [
        "Parallelism": 1
      ],
      "EndpointSpec": [
        "Ports": publish
      ]
    ]

    dockerClient.createService(serviceConfig).content.ID
  }

  def rm(name) {
    dockerClient.rmService("$name")
  }

  def ls(args) {
    dockerClient.services(args).content
  }

  def getLs() {
    dockerClient.services().content
  }

}
