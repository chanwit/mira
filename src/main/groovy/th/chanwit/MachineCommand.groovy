package th.chanwit

import de.gesellix.docker.client.config.*
import groovy.json.JsonSlurper
import java.util.concurrent.*

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

	private static pool = Executors.newFixedThreadPool(10)
	private static ecs  = new ExecutorCompletionService<Void>(pool)

	private static void doWait() {
		pool.shutdown()
		pool.awaitTermination(1, TimeUnit.HOURS)
	}

	def create(Map map, arg) {
		def driver = map['driver']
		def driverMap = map[driver]
		def driverArgs = driverMap.collect { k, v ->
			if(v == false) {
				return []
			} else if (v==true) {
				return ["--$driver-$k"]
			}
			return ["--$driver-$k", "$v"]
		}.flatten()

		def engineMap = map['engine']
		def engineArgs = engineMap.collect { k, v ->
			return ["--engine-$k", "$v"]
		}.flatten()

		def cmd = ["docker-machine", "create", "-d", driver] +
			driverArgs +
			engineArgs +
			["$arg"]

		ecs.submit({
			def proc = cmd.execute()
			proc.waitForProcessOutput( System.out, System.err )
		} as Runnable, Void)
	}

	def rm(arg) {
		def cmd = ["docker-machine", "rm", "-f", "$arg"]
		def proc = cmd.execute()
		proc.waitForProcessOutput( System.out, System.err )
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