fun org.gradle.kotlin.dsl.DependencyHandlerScope.implementation(item: Array<String>) = item.forEach { add("implementation", it) }