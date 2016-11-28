package th.chanwit.plugin

class CalicoPlugin extends AbstractPlugin {

    void init(Binding bindings) {
        // bindings['swarm'] = this.swarm
        println "calico plugin initialized ..."
    }

    @Override
    void afterProvision() {
        println "calico plugin:: afterProvision"
    }

}