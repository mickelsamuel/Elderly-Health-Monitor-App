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
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true // Enable View Binding
    }
}

dependencies {
    // Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:31.1.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    //implementation("com.google.firebase:firebase-analytics:21.0.0")

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
    tasks.named("mapDebugSourceSetPaths") {
        dependsOn(tasks.named("processDebugGoogleServices"))
    }
    tasks.named("mergeDebugResources") {
        dependsOn(tasks.named("processDebugGoogleServices"))
    }
}

// Perform a clean build to ensure changes take effect
tasks.register("cleanBuild") {
    dependsOn("clean", "build")
}
