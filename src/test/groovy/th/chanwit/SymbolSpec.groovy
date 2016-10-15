package th.chanwit

class SymbolSpec extends spock.lang.Specification {

	def "test symbol compare equal"() {
		given:
			def a1 = new Symbol("a1")
			def a2 = new Symbol("a1")
		when:
			def result = (a1 == a2)
		then:
			assert result == true
	}

	def "test symbol not equal"() {
		given:
			def a1 = new Symbol("a1")
			def a2 = new Symbol("a2")
		when:
			def result = (a1 != a2)
		then:
			assert result == true
	}

	def "test negative symbol"() {
		given:
			def param = new Symbol("param")
		when:
			def result = -param
		then:
			assert result == new Symbol("-param")
	}

	def "test hashCode"() {
		when:
			def node1 = new Symbol("node1")
			def node2 = new Symbol("node2")
			def node10 = new Symbol("node10")
		then:
			assert node2.hashCode()  - node1.hashCode() == 1
			assert node10.hashCode() - node1.hashCode() == 9
	}

	def "test symbol range"() {
		given:
			def node1 = new Symbol("node1")
			def node10 = new Symbol("node10")
		when:
			def result = node1..node10
		then:
			assert result.size() == 10
			assert result.get(1) == new Symbol("node2")
			assert result.get(9) == new Symbol("node10")
	}

	def "test symbol range 2"() {
		given:
			def node = new Symbol("node")
		when:
			def result = node[1..10]
		then:
			assert result.size() == 10
			assert result.get(1) == new Symbol("node2")
			assert result.get(9) == new Symbol("node10")
	}

}