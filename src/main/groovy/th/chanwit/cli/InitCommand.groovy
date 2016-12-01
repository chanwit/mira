package th.chanwit.cli

import com.beust.jcommander.Parameter
import com.beust.jcommander.Parameters

@Parameters(commandDescription = "Init Mirafile and repository")
class InitCommand {

    @Parameter(names = ["-f", "--force"], description = "Force overwrite Mirafile")
    Boolean force = false

}
