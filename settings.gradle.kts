plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "hudsonstream"
include("broker-storage")
include("broker-core")
include("client-sdk")