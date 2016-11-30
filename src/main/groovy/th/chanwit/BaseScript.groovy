package th.chanwit

import th.chanwit.plugin.Interceptor

abstract class BaseScript extends Script {

    static ThreadLocal env = new ThreadLocal()
    static ThreadLocal<Interceptor> interceptor = new ThreadLocal<Interceptor>()

    def propertyMissing(String name) {
        return new Symbol(name)
    }

}