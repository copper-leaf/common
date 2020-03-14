plugins {
    id("com.eden.orchidPlugin") version "0.19.0"
}

group = rootProject.group
version = rootProject.version

repositories {
    jcenter()
    maven(url="https://kotlin.bintray.com/kotlinx")
    maven(url="https://dl.bintray.com/javaeden/Orchid/")
    maven(url="https://dl.bintray.com/javaeden/Eden/")
    maven(url="https://jitpack.io")
}

dependencies {
    val orchidVersion = "0.19.0"
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidCore:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidBsDoc:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidJavadoc:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidPluginDocs:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSearch:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidWiki:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidChangelog:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidSyntaxHighlighter:$orchidVersion")
    orchidRuntimeOnly("io.github.javaeden.orchid:OrchidGithub:$orchidVersion")
}

// Javadoc and Orchid
//----------------------------------------------------------------------------------------------------------------------

orchid {
    version = "${project.version}"
    theme = "BsDoc"
    baseUrl = "https://javaeden.github.io/Common"
    githubToken = project.properties["githubToken"]?.toString()

    if(project.hasProperty("env") && project.property("env") == "prod") {
        environment = "prod"
    }
    args = listOf("--experimentalSourceDoc")
}
