plugins {
    kotlin("multiplatform")
    `copper-leaf-base`
    `copper-leaf-version`
    `copper-leaf-lint`
    `copper-leaf-publish`
}

description = "A collection of utility classes and tools used throughout other Orchid and Copper Leaf repositories"

kotlin {
    jvm {
        withJava()
    }

    sourceSets {
        all {
            languageSettings.apply {
                useExperimentalAnnotation("kotlin.Experimental")
            }
        }

        // Common Sourcesets
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common:1.4.32")
            }
        }

        // plain JVM Sourcesets
        val jvmMain by getting {
            dependencies {
                implementation("io.github.copper-leaf:clog-core-jvm:4.1.0")
                implementation("org.json:json:20210307")
                implementation("javax.inject:javax.inject:1")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-junit:1.4.32")
                implementation("io.mockk:mockk:1.11.0")

                implementation("org.hamcrest:hamcrest-library:2.2")
                implementation("org.mockito:mockito-core:3.9.0")
                implementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
                implementation("org.junit.jupiter:junit-jupiter-params:5.7.1")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
            }
        }
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = Config.javaVersion
    targetCompatibility = Config.javaVersion
}
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.useIR = true
    kotlinOptions {
        jvmTarget = Config.javaVersion
    }
}
