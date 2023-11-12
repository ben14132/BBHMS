plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.mygooglemaps"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.mygooglemaps"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        //resourceConfigurations += listOf("en", "ms", "zh-Hans-CN", "zh-Hant-TW")
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

    // Java language implementation
    implementation ("androidx.fragment:fragment:$1.6.1")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation ("com.google.android.material:material:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation ("mysql:mysql-connector-java:5.1.47")
    implementation("androidx.cardview:cardview:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    //autocomplete search
    implementation ("com.google.android.libraries.places:places:3.2.0")

    //cardview
    //implementation ("com.android.support:appcompat-v7:27.1.1")
    //implementation ("com.android.support:cardview-v7:27.1.1")

    //language
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("androidx.appcompat:appcompat-resources:1.6.1")

    //graph library
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    //image
    implementation ("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.14.2")

}



