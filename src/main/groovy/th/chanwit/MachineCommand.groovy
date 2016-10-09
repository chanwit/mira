package th.chanwit

import de.gesellix.docker.client.config.*
import groovy.json.JsonSlurper

class MachineCommand {

	DockerEnv env(Symbol machineName) {
		if ("$machineName" == "-u") {
			DockerEnv e = new DockerEnv()
			BaseScript.envLocal.set(e)
			return e
		}

		String dir = "${System.getProperty('user.home')}/.docker/machine/machines/${machineName}"
		String text = new File(dir, "config.json").text
		def json = new JsonSlurper().parseText(text)
		DockerEnv result = new DockerEnv(
			tlsVerify: "1",
			dockerHost: "tcp://${json['Driver']['IPAddress']}:2376",
			certPath: dir,
		)
		BaseScript.envLocal.set(result)
		return result
	}

	private _env() {
		/*
		String lines = "docker-machine env $machineName".execute().text
		lines.split("\n").each { String line ->
			if (line.startsWith("export")) {
				String[] parts = (line - "export ").split("=")
				String key = parts[0]
				String val = parts[1].replaceAll(/^\"|\"$/, "");
				switch(key) {
					case "DOCKER_TLS_VERIFY": result.tlsVerify = val; break
					case "DOCKER_HOST": result.dockerHost = val; break
					case "DOCKER_CERT_PATH": result.certPath = val; break
				}
			}
		}*/
		// println ">>>|> $result"
	}

}