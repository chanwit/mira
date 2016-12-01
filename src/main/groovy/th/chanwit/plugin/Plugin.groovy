package th.chanwit.plugin

/**
 * Created by chanwit on 11/27/16.
 */
interface Plugin {

    void init(Binding bindings)

    void beforeUp()

    void beforeDown()

    void beforeBuild()

    void beforePush()

    void beforeClean()

    void beforeProvision()

    void afterUp()

    void afterDown()

    void afterBuild()

    void afterPush()

    void afterClean()

    void afterProvision()

}