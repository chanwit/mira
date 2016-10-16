package th.chanwit.plugin

class CalicoPlugin {

	def init(bindings) {
		// bindings['swarm'] = this.swarm
		println "calico plugin initialized ..."
	}

	def afterProvision() {
		println "calico plugin:: afterProvision"
	}

}