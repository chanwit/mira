package th.chanwit.cli

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

@Parameters(commandDescription = "Set active machine repository")
class ActiveCommand {
    @Parameter(description = "cluster name", arity = 1)
    List<String> cluster = []
}
