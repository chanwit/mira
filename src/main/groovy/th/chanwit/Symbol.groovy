package th.chanwit

class Symbol implements Comparable {

    String val

    Symbol(String val) {
        // println ">> $val"
        this.val = val
    }

    def negative() {
        new Symbol("-${val}")
    }

    private split() {
        def m = this.val =~ /([a-zA-Z][a-zA-Z\-]*)(\d+)/
        def prefix = m[0][1]
        def num = Integer.valueOf(m[0][2])
        return [prefix, num]
    }

    int compareTo(java.lang.Object b) {
        try {
            def (prefix_a, num_a) = split()
            def (prefix_b, num_b) = b.split()

            if (prefix_a == prefix_b) {
                return num_a <=> num_b
            }

        } catch (e) {
        }

        return this.val <=> b.val
    }

    def previous() {
        def (prefix, num) = split()
        return new Symbol("$prefix${num - 1}")
    }

    def next() {
        def (prefix, num) = split()
        return new Symbol("$prefix${num + 1}")
    }

    def getAt(IntRange r) {
        r.collect {
            new Symbol("$val$it")
        }
    }

    String toString() {
        return this.val
    }

    int hashCode() {
        try {
            def (prefix, num) = split()
            return prefix.hashCode() + num
        } catch (e) {
            return this.val.hashCode()
        }
    }
}