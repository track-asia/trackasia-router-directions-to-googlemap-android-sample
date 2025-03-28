package com.maps.route.data

import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.maps.route.model.DirectionsResponse
import com.maps.route.model.TravelMode
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.util.Locale

internal class RepositoryDrawRouteImp : RepositoryDrawRoute {

    /**
     * Performs the network operation to fetch directions.
     *
     * @param origin The starting point for the directions.
     * @param destination The destination point for the directions.
     * @param mode The mode of transport (e.g., driving, walking).
     * @param apiKey The directions API
     * @return A Result wrapping the DirectionsResponse or an Exception.
     */
    override suspend fun fetchDirections(
        origin: LatLng,
        destination: LatLng,
        mode: TravelMode,
        apiKey: String
    ): Flow<DataState<DirectionsResponse>> {
        return flow {
            val url = buildUrl(origin, destination, mode.name, apiKey)
            val request = Request.Builder().url(url).build()

            try {
                val response = OkHttpClient().newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected code $response")
                }
                val responseBodyString = response.body()?.string()
                println("Response body: $responseBodyString")
//                response.body()?.let { responseBody ->
//                    val directionsResponse =
//                        Gson().fromJson(responseBody.string(), DirectionsResponse::class.java)
//                    emit(DataState.Success(directionsResponse))
//                } ?: throw IOException("Empty response body")
                if (responseBodyString.isNullOrEmpty()) {
                    throw IOException("Empty response body")
                }

                val directionsResponse = Gson().fromJson(responseBodyString, DirectionsResponse::class.java)
                emit(DataState.Success(directionsResponse))
            } catch (e: Exception) {
                // Rethrow the exception to be caught by the catch operator
                throw e
                println("Response body: ${e.message}")
            }
        }.catch { exception ->
            throw IOException("Unexpected code $exception")
        }.flowOn(Dispatchers.IO)
    }

    /**
     * Constructs the URL for the Directions API request.
     *
     * @param origin The starting point for the directions.
     * @param destination The destination point for the directions.
     * @param mode The mode of transport (e.g., driving, walking).
     * @return The constructed URL string.
     */
    private fun buildUrl(
        origin: LatLng,
        destination: LatLng,
        mode: String,
        apiKey: String
    ): String {
        return ("https://dev.maps.track-asia.com/route/v2/directions/json?"
                + "origin="
                + origin.latitude + ","
                + origin.longitude
                + "&destination="
                + destination.latitude + ","
                + destination.longitude
//                + "&sensor=false&units=metric&mode=${mode.lowercase(Locale.getDefault())}"
                + "&key=$apiKey"
                )
    }
}
