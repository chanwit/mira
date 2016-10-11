package th.chanwit

import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration

import de.gesellix.docker.client.*
import de.gesellix.docker.client.config.*

class MiraMain {

  static DockerClient createClient() {
    DockerEnv env = (DockerEnv)BaseScript.envLocal.get()
    DockerClient cli
    if(env) {
      cli = new DockerClientImpl(env)
    } else {
      cli = new DockerClientImpl()
    }
    return cli
  }

  static curl = {String url ->
    new URL(url).text
  }

  static class Holder {
    def up
    def down
    def build
    def push
  }

  // alias to 'task'
  static define = { MiraAction act, Closure c ->
    switch(act) {
      case MiraAction.up:
        delegate.up = c; break
      case MiraAction.down:
        delegate.down = c; break
      case MiraAction.build:
        delegate.build = c; break
      case MiraAction.push:
        delegate.push = c; break
    }
  }

  // handle sub-commands
  static docker = { MiraAction sub ->
    switch(sub) {
      case MiraAction.service:
        return new ServiceCommand(createClient())
      case MiraAction.machine:
        return new MachineCommand()
      case MiraAction.network:
        return new NetworkCommand(createClient())
      case MiraAction.swarm:
        return new SwarmCommand(createClient())
      // TODO: fixing stuff
      case MiraAction.push:
        return new PushCommand(createClient())
      default:
        // top-level comand, discard
        return null
    }
  }

  static void main(String[] args) {

    // handle top-level commands
    MiraAction.metaClass.call = { sym ->
      switch("$delegate") {
        case "push":
          return new PushCommand(createClient()).push("$sym")
        default:
          throw new Exception("NYI: $delegate")
      }
    }

    // handle top-level commands
    MiraAction.metaClass.call = { Map map, Symbol sym ->
      switch("$delegate") {
        case "build":
          return new BuildCommand(createClient()).build(map, sym)
        default:
          throw new Exception("NYI: $delegate")
      }
    }

    def imports = new ImportCustomizer()
    imports.addStaticStars "th.chanwit.MiraAction"

    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(imports)

    def holder = new Holder()
    define.delegate = holder

    def binding = new Binding(
      docker: docker,
      curl:   curl,
      define: define,
      task:   define,
    )

    config.setScriptBaseClass("th.chanwit.BaseScript")
    def shell = new GroovyShell(binding, config)

    shell.evaluate new File("Mirafile")

    if(args.size() == 0) {
      println "up:"
      holder.up()
    } else {
      // mira build up
      args.each { action ->
        println "$action:"
        holder."${action}"()
      }
    }
  }

}
