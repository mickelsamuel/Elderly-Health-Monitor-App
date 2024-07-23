plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.elderly_health_monitor_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.elderly_health_monitor_app"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:31.1.0"))
    implementation("com.google.firebase:firebase-messaging")

    // AndroidX dependencies
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    implementation("com.jjoe64:graphview:4.2.2")
    implementation("androidx.core:core:1.7.0")

    // Retrofit example, excluding support-compat
    implementation("com.squareup.retrofit2:retrofit:2.9.0") {
        exclude(group = "com.android.support", module = "support-compat")
    }

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}

// Ensure proper task dependencies
afterEvaluate {
    val processDebugGoogleServices by tasks.named("processDebugGoogleServices")
    val mergeDebugResources by tasks.named("mergeDebugResources")

    mergeDebugResources.dependsOn(processDebugGoogleServices)
}

// Perform a clean build to ensure changes take effect
tasks.register("cleanBuild") {
    dependsOn("clean", "build")
}