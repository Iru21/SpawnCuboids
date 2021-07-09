import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
}

group = "me.mateusz"

repositories {
    mavenCentral()
    mavenLocal()
    maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven(url = "https://maven.enginehub.org/repo/")
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.spigotmc:spigot-api:1.17-R0.1-SNAPSHOT")
    implementation("com.sk89q.worldedit:worldedit-bukkit:7.3.0-SNAPSHOT")
    implementation("com.sk89q.worldguard:worldguard-bukkit:7.0.5-SNAPSHOT")
    implementation("com.github.MilkBowl:VaultAPI:1.7")
    implementation(kotlin("stdlib-jdk8"))

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
