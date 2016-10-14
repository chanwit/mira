package th.chanwit.plugin

import groovy.json.*
import de.gesellix.docker.client.*
import de.gesellix.docker.client.config.*

class SwarmModePlugin /* implements Plugin*/  {

	def init(bindings) {
		bindings['swarm'] = this.swarm
	}

	def managers = []
	def workers = []

	class Builder {

		private plugin

		Builder(plugin) {
			this.plugin = plugin
		}

		def manager(Object[] args) {
			managers(args)
		}
		def managers(Object[] args) {
			if(args.size() == 1 && args[0] instanceof Range) {
				args[0].each {
					plugin.managers	<< it
				}
			} else {
				plugin.managers = args
			}
		}

		def worker(Object[] args) {
			workers(args)
		}
		def workers(Object[] args) {
			if(args.size() == 1 && args[0] instanceof Range) {
				args[0].each {
					plugin.workers	<< it
				}
			} else {
				plugin.workers = args
			}
		}
	}

	def swarm = { c ->
		c.delegate = new Builder(this)
		c.resolveStrategy = Closure.DELEGATE_FIRST
		c.call()
	}

	private env(String machineName) {
		String dir = "${System.getProperty('user.home')}/.docker/machine/machines/${machineName}"
		String text = new File(dir, "config.json").text
		def json = new JsonSlurper().parseText(text)
		DockerEnv result = new DockerEnv(
			tlsVerify: "1",
			dockerHost: "tcp://${json['Driver']['IPAddress']}:2376",
			certPath: dir,
		)
		def ip = json['Driver']['IPAddress']
		return [ip, result]
	}

	def afterProvision() {
		def (ip, e) = env("${managers[0]}")
		def cli = new DockerClientImpl(e)
		def config = [
  			"ListenAddr": "0.0.0.0:2377",
  			"AdvertiseAddr": "${ip}:2377",
  			"ForceNewCluster": false,
  			"Spec": [
    			"Orchestration": [:],
    			"Raft": [:],
    			"Dispatcher": [:],
    			"CAConfig": [:],
  			]
		]
		cli.initSwarm(config)
		println "(${managers[0]}) initialized ..."

		def inspect = cli.inspectSwarm().content
		def mg_token = inspect.JoinTokens.Manager
		def wk_token = inspect.JoinTokens.Worker

		if (managers.size() > 1) {
			managers[1..-1].each { mg ->
				def (mg_ip, mg_env) = env("$mg")
				def mg_cli = new DockerClientImpl(mg_env)
				def mg_config = [
	  				"ListenAddr": "0.0.0.0:2377",
	  				"AdvertiseAddr": "${mg_ip}:2377",
	  				"RemoteAddrs": ["${ip}:2377"],
	  				"JoinToken": "$mg_token"
				]
				mg_cli.joinSwarm(mg_config)
				println "(${mg}) joined as manager ..."
			}
		}

		workers.each { wk ->
			def (wk_ip, wk_env) = env("$wk")
			def wk_cli = new DockerClientImpl(wk_env)
			def wk_config = [
  				"ListenAddr": "0.0.0.0:2377",
  				"AdvertiseAddr": "${wk_ip}:2377",
  				"RemoteAddrs": ["${ip}:2377"],
  				"JoinToken": "$wk_token"
			]
			wk_cli.joinSwarm(wk_config)
			println "(${wk}) joined as worker ..."
		}
	}

}