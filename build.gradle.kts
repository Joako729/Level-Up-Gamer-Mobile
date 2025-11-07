plugins {
    id("com.android.application") version "8.6.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}

extra.apply {
    set("compose_bom", "2024.10.01")
    set("nav_version", "2.8.3")
    set("lifecycle_version", "2.8.6")
    set("room_version", "2.6.1")
}
