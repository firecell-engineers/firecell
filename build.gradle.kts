plugins {
    id("java")
}

group = "pl.edu.agh"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

val lwjglVersion = "3.3.1"
val lwjglNatives = Pair(System.getProperty("os.name")!!, System.getProperty("os.arch")!!)
    .let { (name, arch) ->
        when {
            arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
                "natives-linux"
            arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
                "natives-macos"
            arrayOf("Windows").any { name.startsWith(it) } ->
                "natives-windows"
            else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
        }
    }

dependencies {

    // opengl
    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    implementation("org.lwjgl", "lwjgl")
    implementation("org.lwjgl", "lwjgl-glfw")
    implementation("org.lwjgl", "lwjgl-opengl")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)

    // imgui
    implementation(fileTree("lib") { include("*.jar") })

    // slf4j & log4j
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.apache.logging.log4j", "log4j-api", "2.7")
    implementation("org.apache.logging.log4j", "log4j-core", "2.7")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.7")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}