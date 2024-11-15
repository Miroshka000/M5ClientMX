plugins {
    id("java")
    id("application")
    id("org.beryx.jlink") version "3.0.1"
    id("maven-publish")
}

group = "miroshka"
version = "0.3.4"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fazecast:jSerialComm:2.11.0")
    implementation("commons-io:commons-io:2.17.0")
    implementation("org.openjfx:javafx-base:23.0.1:win")
    implementation("org.openjfx:javafx-controls:23.0.1:win")
    implementation("org.openjfx:javafx-fxml:23.0.1:win")
    implementation("org.openjfx:javafx-graphics:23.0.1:win")
    implementation("org.json:json:20240303")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.named<JavaExec>("run") {
    jvmArgs = listOf(
        "--module-path", configurations.runtimeClasspath.get().joinToString(";"),
        "--add-modules", "javafx.controls,javafx.fxml,jdk.crypto.cryptoki",
        "-Dapp.version=${project.version}"
    )
}

tasks.named<ProcessResources>("processResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

application {
    mainClass.set("miroshka.Main")
    mainModule.set("miroshka")
    applicationDefaultJvmArgs = listOf("-Dapp.version=${project.version}")
}

jlink {
    options = listOf("--strip-debug", "--compress=2", "--no-header-files", "--no-man-pages")

    launcher {
        name = "M5Client"
        jvmArgs = listOf(
            "--add-modules", "javafx.controls,javafx.fxml",
            "--module-path", configurations.runtimeClasspath.get().asPath,
            "-Dapp.version=${project.version}"
        )
    }

    jpackage {
        imageName = "M5ClientMX"
        outputDir = "$buildDir/installer"
        installerType = "exe"
        installerOptions = listOf(
            "--win-per-user-install",
            "--win-shortcut",
            "--win-dir-chooser"
        )
        imageOptions = listOf("--icon", "src/main/resources/miroshka/icon.ico")
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

sourceSets {
    main {
        resources.srcDir("src/main/resources")
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            artifact(tasks.named("jpackage"))
        }
    }

    repositories {
        maven {
            url = uri("https://github.com/Miroshka000/M5ClientMX")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.token") as String? ?: System.getenv("TOKEN")
            }
        }
    }
}
