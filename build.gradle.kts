plugins {
    `java-library`
}

repositories {
    // Dependencies for Minestom
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://jitpack.io")
}

val lwjglVersion = "3.2.3"

dependencies {
    // Minestom API
    compileOnly("com.github.Minestom:Minestom:-SNAPSHOT")
    api(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))

    api("org.lwjgl:lwjgl")
    api("org.lwjgl:lwjgl-egl")
    api("org.lwjgl:lwjgl-opengl")
    api("org.lwjgl:lwjgl-opengles")
    api("org.lwjgl:lwjgl-glfw")
    api("org.lwjgl:lwjgl-glfw")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}