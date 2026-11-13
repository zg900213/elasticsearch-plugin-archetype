/*
 * Post-generation hook executed by maven-archetype-plugin 3.x.
 *
 * The REST handler example only makes sense for pluginType=rest. For every
 * other plugin type the placeholder file is removed so the generated project
 * stays clean. The Velocity templates themselves also guard against this case
 * (they emit a comment-only file), so generation still works with older
 * archetype plugins that ignore this script.
 */
import java.io.File

def props = request.properties
def pluginType = (props.getProperty("pluginType") ?: "rest").trim().toLowerCase()
def pluginName = props.getProperty("pluginName") ?: "Hello"
def packageName = props.getProperty("package") ?: ""

if (pluginType != "rest") {
    def root = new File(request.outputDirectory.toString(), request.artifactId.toString())
    def packagePath = packageName.replace('.', '/')
    def restFile = new File(root, "src/main/java/${packagePath}/rest/Rest${pluginName}Action.java")

    if (restFile.exists()) {
        restFile.delete()
    }
    def restDir = restFile.parentFile
    if (restDir != null && restDir.isDirectory() && restDir.list() != null && restDir.list().length == 0) {
        restDir.delete()
    }
}
