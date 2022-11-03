package com.jge.pingmylocation

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class LocationWorker(private val context: Context, params: WorkerParameters):Worker(context, params) {

    override fun doWork(): Result {
        /**FusedLocationProviderClient is best recommended by Android
         * since we can use this to always get the user's updated location**/
        val locationClient = DefaultLocationClient(applicationContext, LocationServices.getFusedLocationProviderClient(applicationContext))
        return try{
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                var interval = 1000L * 60 * 60
                if(BuildConfig.DEBUG){
                    interval = 1000L * 60
                }
                locationClient.getLocationUpdates(interval)
                    .catch { e->Result.failure() }
                    .onEach {
                        val lat = it.latitude.toString()
                        val long = it.longitude.toString()
                        Toast.makeText(context,"Location: ($lat, $long)", Toast.LENGTH_SHORT).show()
                        //TODO -TASK #5:To persist location into storage we could use either SharedPreferences or Room Library
                    }.launchIn(MainScope())
            }, 1000)

            Result.success()
        }catch(throwable:Throwable){
            Result.failure()
        }
    }
}