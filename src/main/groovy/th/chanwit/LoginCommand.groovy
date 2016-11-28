package th.chanwit

import de.gesellix.docker.client.DockerClient
import java.io.Console

class LoginCommand {

    private DockerClient dockerClient

    LoginCommand(DockerClient cli) {
        this.dockerClient = cli
    }

    def login() {
        def cons = System.console()
        cons.print("username: ")
        def username = cons.readLine()
        cons.print("password: ")
        def password = cons.readPassword()
        def authDetails = ["username"     : username,
                           "password"     : password,
                           // "email"        : dockerHubEmail,
                           "serveraddress": "https://index.docker.io/v1/"]
        dockerClient.auth(authDetails)
        // dockerClient.readAuthConfig()
    }


}
