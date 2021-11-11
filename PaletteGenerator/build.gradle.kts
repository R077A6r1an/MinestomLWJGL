plugins {
    java
    application
}

repositories {
    // Dependencies for Minestom
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
}

application {
    mainClass.set("PaletteGenerator")
}

val lwjglVersion = "3.2.3"

dependencies {
    // Minestom API
    implementation("com.github.Minestom:Minestom:-SNAPSHOT")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    getByName<JavaExec>("run") {
        args = listOf(
            // src/main/resources/palette.png
            rootProject.file("src").toPath().resolve("main").resolve("resources").resolve("textures")
                .resolve("palette.png").toString()
        )
    }
}