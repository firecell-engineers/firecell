import com.google.protobuf.gradle.*

plugins {
    id("java")
    id("com.google.protobuf") version "0.8.18"
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
    // junit tests
    testImplementation(group = "junit", name = "junit", version = "4.13")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", "5.6.2")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", "5.6.2")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine", "5.6.2")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", "5.6.2")

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

    // protobuf
    implementation("com.google.protobuf", "protobuf-java", "3.21.1")

    // rxjava
    implementation("io.reactivex.rxjava3", "rxjava", "3.1.5")

    // joml
    implementation("org.joml:joml:1.10.4")

    // apache commons
    implementation("org.apache.commons:commons-lang3:3.12.0")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.withType<Jar> { duplicatesStrategy = DuplicatesStrategy.INHERIT }

sourceSets {
    main {
        java {
            srcDirs("build/generated/source/proto/main/java")
        }
    }
}

protobuf {
    // Configure the protoc executable
    protoc {
        // Download from repositories
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
}