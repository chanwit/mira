package th.chanwit

class ServiceCommand {

  def create(Map map, String args="") {
    println "$map $args"
  }

  def ls(args) {
    println "ls $args"
  }

  def getLs() {
    println "ls"
  }

}
