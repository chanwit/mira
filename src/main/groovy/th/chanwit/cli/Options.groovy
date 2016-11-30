package th.chanwit.cli

import com.beust.jcommander.Parameter

class Options {
    @Parameter(names = ["-f", "--file"], description = "Specify a custom Mirafile")
    String filename = "Mirafile"

    @Parameter(names = ["-h", "--help"], help = true, description = 'Show usage information')
    Boolean help

    @Parameter(description = "[tasks ...]")
    List<String> tasks = ['up']
}
