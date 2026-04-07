// ===================================================================
// PUBLISH TO NEXUS
// ===================================================================
// after ./gradlew :Common:build or ./gradlew clean build
// then use ./gradlew :Common:publish or ./gradlew publish to up jar to nexus
// if not nexus will create new version + "-number++" like ( common.jar -> common-1.jar -> common-2.jar)

//Gradle loads properties in the following order (from lowest to highest priority):
//gradle.properties (project-level or ~/.gradle/gradle.properties)
//System properties (-Dkey=value)
//Environment variables (System.getenv)
//Manual overrides in build.gradle via project.ext.set(...)
//CLI -Pkey=value properties
//publishing {
//    publications {
//        create("mavenJava", MavenPublication) {
//            from components.java
//        }
//    }
//
//    repositories {
//        maven {
//            url = getProp("SNAPSHOT_REPOSITORY_URL")
//            credentials {
//                username = getProp("NEXUS_USERNAME")
//                password = getProp("NEXUS_PASSWORD")
//            }
//        }
//    }
//}
//
//def getProp(String name) {
//    return project.hasProperty(name) ? project.property(name).toString() : System.getenv(name)
//}

// Trong file publishing.gradle.kts
val snapshotUrl = project.findProperty("SNAPSHOT_REPOSITORY_URL")?.toString() ?: System.getenv("SNAPSHOT_REPOSITORY_URL")

// extensions.configure danh cho cac tac vu co trong plugins vi du o day la plugins maven-publish
extensions.configure<org.gradle.api.publish.PublishingExtension>("publishing") {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri(snapshotUrl ?: "")
            credentials {
                username = project.findProperty("NEXUS_USERNAME")?.toString() ?: System.getenv("NEXUS_USERNAME")
                password = project.findProperty("NEXUS_PASSWORD")?.toString() ?: System.getenv("NEXUS_PASSWORD")
            }
        }
    }
}
