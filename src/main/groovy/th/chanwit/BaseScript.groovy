package th.chanwit

abstract class BaseScript extends Script {

	static ThreadLocal envLocal = new ThreadLocal();

	def propertyMissing(String name) {
        return new Symbol(name)
    }

}