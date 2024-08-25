
plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-lang3:3.14.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("commons-validator:commons-validator:1.8.0")
    /* module 2.1 -- Vert.x */
    implementation("io.vertx:vertx-core:4.5.7")
    implementation("io.vertx:vertx-web:4.5.7")
    implementation("io.vertx:vertx-web-client:4.5.7")

    /* module 2.2 -- RxJava */
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")

    runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.72.Final:osx-aarch_64")

    implementation("org.springframework:spring-web:6.1.6")

    implementation("org.apache.commons:commons-collections4:4.4")

}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

task("runEventDriven", JavaExec::class) {
    mainClass.set("crawler.event_driven.Launcher")
    classpath = sourceSets["main"].runtimeClasspath
}

task("runRx", JavaExec::class) {
    mainClass.set("crawler.reactive.Launcher")
    classpath = sourceSets["main"].runtimeClasspath
}

task("runVirtualThread", JavaExec::class) {
    mainClass.set("crawler.virtual_threads.Launcher")
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.test {
    useJUnitPlatform()
}