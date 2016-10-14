package th.chanwit

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode
class Symbol implements Comparable {

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

    int compareTo(java.lang.Object b) {
		return this.val.compareTo(b.val)
    }

    def next() {
		def m = this.val =~ /(\w+)(\d+)/
		def prefix = m[0][1]
		def num = Integer.valueOf(m[0][2])
		return new Symbol("$prefix${num+1}")
    }

    def getAt(IntRange r) {
    	r.collect {
      		new Symbol("$val$it")
    	}
  	}

	String toString() {
		return this.val
	}
}