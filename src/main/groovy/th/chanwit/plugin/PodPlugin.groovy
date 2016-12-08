package th.chanwit.plugin

import th.chanwit.Symbol

class PodPlugin extends AbstractPlugin implements Interceptor {

    String prefix

    @Override
    void init(Binding bindings) {
        bindings['pod'] = this.pod
    }

    class Builder {

        private plugin

        Builder(plugin) {
            this.plugin = plugin
        }

        def prefix(String arg) {
            plugin.prefix = arg
        }
    }

    def pod = { c ->
        c.delegate = new Builder(this)
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()
    }

    @Override
    def beforeNetworkCreate(Map map, Symbol arg) {
        return [map, new Symbol("${prefix}_${arg}")]
    }

    @Override
    def beforeNetworkRm(Symbol arg) {
        return new Symbol("${prefix}_${arg}")
    }

    @Override
    def beforeServiceCreate(Map map, String args) {
        def name = map['imageName']
        map['imageName'] = "${prefix}_$name"
        return [map, args]
    }

    @Override
    def beforeServiceRm(String name) {
        return "${prefix}_${name}"
    }

    @Override
    def beforeMachineCreate(String image) {
        return null
    }

}
