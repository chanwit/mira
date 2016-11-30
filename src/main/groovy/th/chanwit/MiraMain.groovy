package th.chanwit

import com.beust.jcommander.JCommander
import de.gesellix.docker.client.DockerClient
import de.gesellix.docker.client.DockerClientImpl
import de.gesellix.docker.client.config.DockerEnv
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import th.chanwit.cli.ActiveCommand
import th.chanwit.cli.InitCommand
import th.chanwit.cli.Options
import th.chanwit.plugin.*

class MiraMain {

    static DockerClient createClient() {
        DockerEnv env = (DockerEnv) BaseScript.env.get()
        DockerClient cli
        if (env) {
            cli = new DockerClientImpl(env)
        } else {
            cli = new DockerClientImpl()
        }
        return cli
    }

    static curl = { String url ->
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
        switch (sub) {
            case MiraAction.service:
                return new ServiceCommand(createClient())
            case MiraAction.machine:
                return new MachineCommand()
            case MiraAction.network:
                return new NetworkCommand(createClient())
            case MiraAction.swarm:
                return new SwarmCommand(createClient())

        // TODO: fixing stuff
        // TOP-level command
            case MiraAction.push:
                return new PushCommand(createClient())
        // TOP-level command
            case MiraAction.info:
                return new InfoCommand(createClient()).info()
            default:
                // top-level command, discard
                return null
        }
    }

    static void main(String[] args) {
        def options = new Options()
        def jc = new JCommander(options)
        def init = new InitCommand()
        jc.addCommand("init", init)
        def active = new ActiveCommand()
        jc.addCommand("active", active)

        jc.parse(args)

        if(options.help) {
            jc.usage()
            return
        }

        // handle top-level commands
        MiraAction.metaClass.call = { Symbol sym ->
            switch ("$delegate") {
                case "push":
                    return new PushCommand(createClient()).push("$sym")
                default:
                    throw new Exception("NYI: $delegate")
            }
        }

        // handle top-level commands
        MiraAction.metaClass.call = { Map map, Symbol sym ->
            switch ("$delegate") {
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

        def DEFAULT_FILE = "Mirafile"

        def include = { Map map ->
            def incFile = map['template']
            def binding = delegate
            def incShell = new GroovyShell(binding, config)
            def miraFilePath = new File(DEFAULT_FILE).absoluteFile.parent
            incShell.evaluate new File("${miraFilePath}/${incFile}.mira")
        }

        def pluginClasses = [
                "swarm" : SwarmModePlugin,
                "calico": CalicoPlugin,
                "pod"   : PodPlugin,
        ]
        def plugins = []

        def apply = { Map map ->
            def binding = delegate
            def pluginName = map['plugin']
            Class<Plugin> cls = pluginClasses[pluginName]
            Plugin p = cls.newInstance()
            p.init(binding)
            plugins << p
            if (p instanceof Interceptor) {
                BaseScript.interceptor.set(p)
            }
        }

        def binding = new Binding(
                docker: docker,
                curl: curl,
                define: define,
                task: define,
                include: include,
                apply: apply,
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

        if (options.filename) {
            DEFAULT_FILE = options.filename
        }

        switch (jc.getParsedCommand()) {
            case "init":
                if (new File(".mira").mkdir() == true) {
                    new File(".mira/active").write("default\n")
                }
                return
            case "active":
                if (active.cluster.size() >= 1) {
                    new File(".mira/active").write("${active.cluster[0]}\n")
                    println(active.cluster[0])
                } else {
                    println(new File(".mira/active").text.trim())
                }
                return
        }

        List actions = options.tasks

        try {
            shell.evaluate new File(DEFAULT_FILE)
        } catch(FileNotFoundException e) {
            println("File not found: $DEFAULT_FILE")
            return
        }

        // check if each action is defined
        actions.each { String action ->
            if (holder."$action" == null) {
                println "Task $action is not defined."
                return
            }
        }

        try {
            // mira build up
            actions.each { String action ->
                println "$action:"
                plugins*."before${action.capitalize()}"()

                holder."${action}"()

                plugins*."after${action.capitalize()}"()
            }

        }
        catch (AssertionError e) {
            e.stackTrace.each {
                if (it.fileName == "Mirafile" && it.lineNumber != -1) {
                    println(it.fileName + ":" + it.lineNumber)
                }
            }
            println e.getMessage()
        }
    }
}

