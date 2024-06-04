package com.example.plantaura2.data

import android.app.Application
import com.google.auth.oauth2.GoogleCredentials
import java.io.InputStream

class GoogleCloudAuthHelper(private val application: Application) {

    fun fetchAuthToken(): String? {
        val inputStream: InputStream = application.assets.open("plantaura2-firebase-adminsdk-ugs06-e8517597f1.json")
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))

        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }
}
