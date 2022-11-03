package com.jge.pingmylocation

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
):LocationClient {
    /**We'll get back the Flow here so we can always update either a view or local storage of the location**/
    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return callbackFlow {
            if(!PermissionsUtil.checkIfUserLocationIsGranted(context)){
                throw LocationClient.LocationException("Location Permissions not granted")
            }

            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if(!gpsEnabled && !networkEnabled){
                throw LocationClient.LocationException("GPS is disabled")
            }
            val locationRequest = LocationRequest.create()

            val locationCallback = object : LocationCallback(){
                override fun onLocationResult(locationResult: LocationResult) {
                    super.onLocationResult(locationResult)
                    locationResult.locations.lastOrNull()?.let{
                        launch {
                            send(it)
                        }
                    }
                }
            }

            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            awaitClose{
                client.removeLocationUpdates(locationCallback)
            }
        }
    }
}