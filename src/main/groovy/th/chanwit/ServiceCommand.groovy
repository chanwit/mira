package th.chanwit

class ServiceCommand {

  def create(Map map, String args="") {
    if (!map["image"]) throw new Exception("service create: no --image specified")
    println "$map $args"
  }

  def ls(args) {
    println "ls $args"
  }

  def getLs() {
    println "ls"
  }

}
