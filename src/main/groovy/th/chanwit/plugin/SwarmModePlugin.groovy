package th.chanwit.plugin

import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.config.DockerEnv
import groovy.json.JsonSlurper
import th.chanwit.MachineCommand

class SwarmModePlugin extends AbstractPlugin {

    void init(Binding bindings) {
        bindings['swarm'] = this.swarm
        // println "swarm plugin initialized ..."
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
            args.each {
                if (it instanceof Range) {
                    it.each { m ->
                        plugin.managers << m
                    }
                } else if (it instanceof List) {
                    plugin.managers += it
                } else {
                    plugin.managers << it
                }
            }
        }

        def worker(Object[] args) {
            workers(args)
        }

        def workers(Object[] args) {
            args.each {
                if (it instanceof Range) {
                    it.each { w ->
                        plugin.workers << w
                    }
                } else if (it instanceof List) {
                    plugin.workers += it
                } else {
                    plugin.workers << it
                }
            }
        }
    }

    def swarm = { c ->
        c.delegate = new Builder(this)
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
    }

    private env(String machineName) {
        if (machineName == null) {
            return []
        }

        String repo = new File(".mira/active_repo").text.trim()

        String dir = ".mira/$repo/machines/${machineName}"
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

    @Override
    void afterProvision() {
        // pre-condition

        _afterProvision()

        // post-condition
        // all manager must present
        // all worker must present
    }

    private void _afterProvision() {
        if (managers.size() == 0) {
            return
        }

        def (ip, e) = env("${managers[0]}")
        def cli = new DockerClientImpl(e)
        def config = [
                "ListenAddr"     : "0.0.0.0:2377",
                "AdvertiseAddr"  : "${ip}:2377",
                "ForceNewCluster": false,
                "Spec"           : [
                        "Orchestration": [:],
                        "Raft"         : [:],
                        "Dispatcher"   : [:],
                        "CAConfig"     : [:],
                ]
        ]
        cli.initSwarm(config)
        println "(${managers[0]}) initialized swarm cluster ..."



        def inspect = cli.inspectSwarm().content
        def mg_token = inspect.JoinTokens.Manager
        def wk_token = inspect.JoinTokens.Worker

        if (managers.size() > 1) {
            managers[1..-1].each { mg ->
                def (mg_ip, mg_env) = env("$mg")
                def mg_cli = new DockerClientImpl(mg_env)
                def mg_config = [
                        "ListenAddr"   : "0.0.0.0:2377",
                        "AdvertiseAddr": "${mg_ip}:2377",
                        "RemoteAddrs"  : ["${ip}:2377"],
                        "JoinToken"    : "$mg_token"
                ]
                mg_cli.joinSwarm(mg_config)
                println "(${mg}) joined as manager ..."
            }
        }

        workers.each { wk ->
            def (wk_ip, wk_env) = env("$wk")
            def wk_cli = new DockerClientImpl(wk_env)
            def wk_config = [
                    "ListenAddr"   : "0.0.0.0:2377",
                    "AdvertiseAddr": "${wk_ip}:2377",
                    "RemoteAddrs"  : ["${ip}:2377"],
                    "JoinToken"    : "$wk_token"
            ]
            wk_cli.joinSwarm(wk_config)
            println "(${wk}) joined as worker ..."
        }
    }

    @Override
    void beforeUp() {
        if (managers.size() > 0) {
            println "(${managers[0]}) set as docker host by swarm plugin"
            new MachineCommand().env(managers[0])
        }
    }

    @Override
    void beforeDown() {
        if (managers.size() > 0) {
            println "(${managers[0]}) set as docker host by swarm plugin"
            new MachineCommand().env(managers[0])
        }
    }
}