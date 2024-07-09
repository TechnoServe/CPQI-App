
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.technoserve.cpqi"
    compileSdk = 34


    defaultConfig {
        applicationId = "com.technoserve.cpqi"
        minSdk = 24
        targetSdk = 34
        versionCode = 9
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations += "en"
        resourceConfigurations += "rw"
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity-ktx:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.05.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    implementation ("androidx.fragment:fragment-ktx:1.7.1")
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.github.skydoves:powerspinner:1.2.4")
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation ("com.github.f0ris.sweetalert:library:1.6.0")
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.github.smarteist:autoimageslider:1.3.9")
    implementation ("com.opencsv:opencsv:5.5.1")
    implementation ("com.toptoche.searchablespinner:searchablespinnerlibrary:1.3.1")
    implementation ("com.androidplot:androidplot-core:1.5.10")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    implementation("androidx.room:room-ktx:$roomVersion")

    implementation("androidx.room:room-rxjava2:$roomVersion")

    implementation("androidx.room:room-rxjava3:$roomVersion")

    implementation("androidx.room:room-guava:$roomVersion")

    testImplementation("androidx.room:room-testing:$roomVersion")

    implementation("androidx.room:room-paging:$roomVersion")
}