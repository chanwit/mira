package th.chanwit.plugin

class CalicoPlugin implements Plugin {

    void init(Binding bindings) {
        // bindings['swarm'] = this.swarm
        println "calico plugin initialized ..."
    }

    void afterProvision() {
        println "calico plugin:: afterProvision"
    }

}