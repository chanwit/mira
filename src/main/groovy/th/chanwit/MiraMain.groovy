package th.chanwit

import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration

import de.gesellix.docker.client.*
import de.gesellix.docker.client.config.*

class MiraMain {

  static docker = { SubCommand sub ->
    switch(sub) {
        case SubCommand.service:
          DockerEnv env = (DockerEnv)BaseScript.envLocal.get()
          DockerClient cli
          if(env) {
            cli = new DockerClientImpl(env)
          } else {
            cli = new DockerClientImpl()
          }
          return new ServiceCommand(cli)
        case SubCommand.machine:
          return new MachineCommand()
    }
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

  static void main(String[] args) {
    def imports = new ImportCustomizer()
    imports.addStaticStars "th.chanwit.MiraAction"
    imports.addStaticStars "th.chanwit.SubCommand"

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

    def action = "up"
    if(args.size() >= 1) {
      action = args[0]
    }

    holder."${action}"()
  }

}
