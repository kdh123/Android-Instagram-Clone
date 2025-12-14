pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "InstagramClone"
include(":app")
include(":core")
include(":core:designsystem")
include(":feature")
include(":feature:home")
include(":feature:search")
include(":feature:add")
include(":feature:reels")
include(":feature:profile")
include(":feature:login")
include(":core:domain")
include(":core:domain:domain-login")
include(":core:data")
include(":core:data:data-common")
include(":core:data:data-login")
