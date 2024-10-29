plugins {
    id("java")
    id("application")
    id("edu.sc.seis.launch4j") version "2.5.0"
}

group = "miroshka"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fazecast:jSerialComm:2.7.0")
    implementation("org.apache.commons:commons-io:1.3.2")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("miroshka.Main")
}

tasks.test {
    useJUnitPlatform()
}

launch4j {
    mainClassName = "miroshka.Main"
    jar = "build/libs/${project.name}-${project.version}.jar"
    outfile = "${project.name}.exe"
    jreMinVersion = "11"
    headerType = "gui"
    icon = "src/main/resources/icon.ico"
}

tasks.named("launch4j").configure {
    dependsOn(tasks.named("jar"))
}
