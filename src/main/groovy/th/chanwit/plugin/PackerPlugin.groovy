package th.chanwit.plugin

import com.myjeeva.digitalocean.DigitalOcean
import com.myjeeva.digitalocean.impl.DigitalOceanClient
import com.myjeeva.digitalocean.pojo.Image
import com.myjeeva.digitalocean.pojo.Images
import th.chanwit.Symbol

import static groovy.json.JsonOutput.prettyPrint
import static groovy.json.JsonOutput.toJson

class PackerPlugin extends AbstractPlugin implements Interceptor {
    /**
     * packer(digitalocean) {*     token
     *     sh 'sudo apt-get install ...'
     *}*
     */

    @Override
    void init(Binding bindings) {
        bindings['packer'] = packer
        println("initialized packer ...")
    }

    String token = ""
    String imageName = ""
    Integer imageID = 0

    def packer = { Symbol symbol, Closure c ->
        println("calling packer ...")
        def b
        switch ("$symbol") {
            case "digitalocean":
                b = new DigitalOceanBuilder(this)
                break
            default:
                throw new Exception("NYI")
        }
        c.delegate = b
        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.call()

        json['builders'] = [b.builder]
        json['provisioners'] = b.provisioners
    }

    private json = [:]

    class DigitalOceanBuilder {
        private PackerPlugin plugin
        private builder = [
                "type"  : "digitalocean",
                "image" : "ubuntu-16-04-x64",
                "region": "nyc3",
                "size"  : "512mb",
        ]
        private provisioners = []

        DigitalOceanBuilder(PackerPlugin plugin) {
            this.plugin = plugin
            println "activating do builder ..."
        }

        def name(val) {
            builder["snapshot_name"] = val
            plugin.imageName = val
        }

        def token(val) {
            builder["api_token"] = val
            plugin.token = val
        }

        def image(val) {
            builder["image"] = val
        }

        def region(val) {
            builder["region"] = val
        }

        def size(val) {
            builder["size"] = val
        }

        def sh(str) {
            provisioners << [type: 'shell', inline: [str]]
        }
    }

    private Integer getImageByName(String name) {
        DigitalOcean apiClient = new DigitalOceanClient(this.token)
        Images result = apiClient.getUserImages(0, 1000)
        Image im = result.images.find { Image im ->
            im.snapshot == true && im.name == name
        }
        if(im) {
            return im.id
        }

        return null
    }

    @Override
    void beforeProvision() {

        Integer id = getImageByName(this.imageName)

        if (id == null) {
            new File(".mira/__BUILD.json").write(prettyPrint(toJson(json)))
            def proc = "packer build -machine-readable .mira/__BUILD.json".execute()
            proc.waitForProcessOutput(System.out, System.err)

            if (proc.exitValue() == 0) {
                id = getImageByName(this.imageName)
            }

        }

        this.imageID = id
    }

    @Override
    def beforeNetworkCreate(Map map, Symbol arg) {
        return null
    }

    @Override
    def beforeNetworkRm(Symbol arg) {
        return null
    }

    @Override
    def beforeServiceCreate(Map map, String arg) {
        return null
    }

    @Override
    def beforeServiceRm(String arg) {
        return null
    }

    @Override
    def beforeMachineCreate(String image) {
        // lookup user image
        Integer id = getImageByName(image)
        if (id == null) {
            return image
        }

        return id.toString()
    }

}
