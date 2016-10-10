package th.chanwit

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Symbol {

	String val

	Symbol(String val) {
		// println ">> $val"
		this.val = val
	}

	def negative() {
		new Symbol("-${val}")
	}

	def previous() {
		new Symbol("--${val}")
	}

	String toString() {
		return this.val
	}
}