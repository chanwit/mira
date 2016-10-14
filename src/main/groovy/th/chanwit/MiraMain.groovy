package th.chanwit

import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration

import de.gesellix.docker.client.*
import de.gesellix.docker.client.config.*

import th.chanwit.plugin.SwarmModePlugin

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

    def provision
    def clean
  }

  // alias to 'task'
  static define = { MiraAction act, Closure c ->
    delegate."$act" = c
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
    MiraAction.metaClass.call = { Symbol sym ->
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

    def include = { Map map ->
      def incFile  = map['template']
      def binding  = delegate
      def incShell = new GroovyShell(binding, config)
      incShell.evaluate new File("${incFile}.tmpl")
    }

    def plugins = []

    def apply = { Map map ->
      def binding = delegate
      switch(map['plugin']) {
        case 'swarm':
          def p = new SwarmModePlugin()
          p.init(binding)
          plugins << p
          break
      }
    }

    def binding = new Binding(
      docker:  docker,
      curl:    curl,
      define:  define,
      task:    define,
      include: include,
      apply:   apply,
    )
    include.delegate = binding
    apply.delegate = binding

    // if action got call with closure,
    // it should be delegate to plugin
    MiraAction.metaClass.call = { Closure c ->
      def action = binding["$delegate"]
      action.call(c)
    }

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
        switch("$action") {
          case "up":
            for(p in plugins) {
              p.beforeUp()
            }
            break
          case "down":
            for(p in plugins) {
              p.beforeDown()
            }
            break
        }
        holder."${action}"()
        switch("$action") {
          case "provision":
            MachineCommand.doWait()
            for(p in plugins) {
              p.afterProvision()
            }
            break
        }
      }
    }
  }

}
