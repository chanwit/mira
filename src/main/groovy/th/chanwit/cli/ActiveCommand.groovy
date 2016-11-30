package th.chanwit.cli

import com.beust.jcommander.Parameter

class ActiveCommand {
    @Parameter(description = "cluster name", arity=1)
    List<String> cluster
}
