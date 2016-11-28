package th.chanwit

import com.aestasit.infrastructure.ssh.SshException
import com.aestasit.infrastructure.ssh.SshOptions
import com.aestasit.infrastructure.ssh.dsl.SshDslEngine
import de.gesellix.docker.client.config.DockerEnv
import groovy.json.JsonSlurper

import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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

    def create(Map map, arg) {
        def pool = Executors.newFixedThreadPool(10)
        def ecs = new ExecutorCompletionService<Void>(pool)

        if (arg instanceof List) {
            arg.each {
                _create(map, it, ecs)
            }
        } else {
            _create(map, arg, ecs)
        }

        pool.shutdown()
        pool.awaitTermination(1, TimeUnit.HOURS)
    }

    def _create(Map map, arg, ExecutorCompletionService<Void> ecs) {
        def driver = map['driver']
        def driverMap = map[driver]
        def driverArgs = driverMap.collect { k, v ->
            if (v == false) {
                return []
            } else if (v == true) {
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
            proc.waitForProcessOutput(System.out, System.err)
        } as Runnable, Void)
    }

    def rm(arg) {
        if (arg instanceof List) {
            arg.each {
                _rm(it)
            }
        } else {
            _rm(arg)
        }
    }

    def _rm(arg) {
        def cmd = ["docker-machine", "rm", "-f", "$arg"]
        def proc = cmd.execute()
        proc.waitForProcessOutput(System.out, System.err)
    }

    def ssh(machine, cl) {
        String dir = "${System.getProperty('user.home')}/.docker/machine/machines/${machine}"
        String text = new File(dir, "config.json").text
        def json = new JsonSlurper().parseText(text)
        def ip = json['Driver']['IPAddress']
        def user = json['Driver']['SSHUser']
        def port = json['Driver']['SSHPort']
        def keyPath = json['Driver']['SSHKeyPath']
        def driverName = json['DriverName']

        def options = new SshOptions()
        options.with {
            defaultHost = ip
            defaultUser = user
            // fix machine 0.9-rc bug
            if(driverName == 'virtualbox') {
                defaultPort = 22
            } else {
                defaultPort = port
            }
            defaultKeyFile = new File(keyPath)
            trustUnknownHosts = true
        }

        def engine = new SshDslEngine(options)
        try {
            engine.remoteSession(cl)
        }catch(SshException e) {
            println(e.getMessage())
        }
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