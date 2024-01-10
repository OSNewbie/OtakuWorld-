package plugins

import AppInfo
import androidx.baselineprofile.gradle.producer.BaselineProfileProducerExtension
import com.android.build.api.dsl.BaselineProfile
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.plugins
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Locale
import kotlin.reflect.KClass

class BenchmarkPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.pluginManager.apply {
            apply("kotlin-android")
            apply("com.android.test")
            apply("androidx.baselineprofile")
        }
        target.tasks.withType<KotlinCompile> { kotlinOptions { jvmTarget = "1.8" } }
        target.configureAndroidBase()
    }

    private fun Project.configureAndroidBase() {
        extensions.findByType(TestExtension::class)?.apply {
            compileSdk = AppInfo.compileVersion

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            defaultConfig {
                minSdk = 28
                targetSdk = 34

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            flavorDimensions += listOf("version")
            productFlavors {
                create("noFirebase") { dimension = "version" }
                create("full") { dimension = "version" }
            }

            dependencies {
                implementation(libs.findLibrary("junit").get())
                implementation(libs.findLibrary("uiautomator").get())
                implementation(libs.findLibrary("espresso-core").get())
                implementation(libs.findLibrary("benchmark-macro-junit4").get())
            }
        }
        extensions.findByType(BaselineProfileProducerExtension::class)?.apply {
            // This is the configuration block for the Baseline Profile plugin.
            // You can specify to run the generators on a managed devices or connected devices.
            useConnectedDevices = true
        }
    }
}