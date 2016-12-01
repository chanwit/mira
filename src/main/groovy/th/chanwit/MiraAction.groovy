package th.chanwit

enum MiraAction {
    service,
    machine,
    network,
    swarm,

    build,
    up,
    down,
    push,
    info,
    provision,
    clean,

    // special action as the return value
            error
}