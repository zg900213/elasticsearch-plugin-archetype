/*
 * Post-generation hook executed by maven-archetype-plugin 3.x.
 *
 * 1. pluginType: the REST handler example only makes sense for pluginType=rest.
 * 2. buildTool:  maven / gradle / both keep only the build files they need.
 *
 * Velocity templates also guard the REST case (they emit a comment-only file),
 * so generation still produces compilable code with older archetype plugins
 * that ignore this script.
 */
import java.io.File

def props = request.properties
def pluginType = (props.getProperty("pluginType") ?: "rest").trim().toLowerCase()
def buildTool = (props.getProperty("buildTool") ?: "both").trim().toLowerCase()
def pluginName = props.getProperty("pluginName") ?: "Hello"
def packageName = props.getProperty("package") ?: ""

def root = new File(request.outputDirectory.toString(), request.artifactId.toString())

def deleteTree
deleteTree = { File target ->
    if (target == null || !target.exists()) {
        return
    }
    if (target.isDirectory()) {
        target.listFiles()?.each { deleteTree(it) }
    }
    target.delete()
}
def remove = { String path -> deleteTree(new File(root, path)) }
def removeIfEmpty = { String path ->
    def dir = new File(root, path)
    if (dir.isDirectory() && dir.list() != null && dir.list().length == 0) {
        dir.delete()
    }
}

if (pluginType != "rest") {
    def packagePath = packageName.replace('.', '/')
    remove("src/main/java/${packagePath}/rest/Rest${pluginName}Action.java")
    removeIfEmpty("src/main/java/${packagePath}/rest")
}

if (buildTool == "maven") {
    ["build.gradle", "settings.gradle", "gradle.properties", "gradlew", "gradlew.bat"].each { remove(it) }
    remove("gradle")
    remove("src/javaRestTest")
} else if (buildTool == "gradle") {
    remove("pom.xml")
    remove("src/main/assemblies")
    remove("src/main/plugin-metadata/plugin-descriptor.properties.tmpl")
    remove("src/test")
    removeIfEmpty("src/main/plugin-metadata")
}
