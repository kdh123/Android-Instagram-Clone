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
include(":core:common")
include(":feature:main")
include(":core:domain:domain-user")
include(":core:data:data-user")
include(":core:ui")
include(":core:domain:domain-feed")
include(":core:data:data-feed")
include(":core:domain:domain-common")
include(":core:database")
include(":feature:feed-common")
include(":core:network")
include(":core:data:data-reels")
include(":core:domain:domain-reels")
