package th.chanwit

import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.CompilerConfiguration


@groovy.transform.CompileStatic
class MiraMain {

  static void main(String[] args) {
    def imports = new ImportCustomizer()
    imports.addStaticStars "th.chanwit.SubCommand"

    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(imports)

    def docker = { SubCommand sub ->
      switch(sub) {
          case SubCommand.service: return new ServiceCommand()
      }
    }

    def binding = new Binding(docker: docker)    

    def shell = new GroovyShell(binding, config)

    shell.evaluate new File("Mirafile")
  }

}
