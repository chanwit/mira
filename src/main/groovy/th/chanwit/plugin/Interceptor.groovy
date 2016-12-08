package th.chanwit.plugin

import th.chanwit.Symbol

interface Interceptor {

    def beforeNetworkCreate(Map map, Symbol arg)

    def beforeNetworkRm(Symbol arg)

    def beforeServiceCreate(Map map, String arg)

    def beforeServiceRm(String arg)

    def beforeMachineCreate(String image)

}