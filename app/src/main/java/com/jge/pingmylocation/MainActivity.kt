package com.jge.pingmylocation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.jge.pingmylocation.databinding.ActivityMainBinding
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var textView: TextView
    private val LOCATION_REQUEST_CODE = 143
    private lateinit var workManager:WorkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //WorkManager is best to use because its highly recommended when doing periodic work
        //Its also best because it handles the battery optimization and it can keep going even when the device restarts
        workManager = WorkManager.getInstance(applicationContext)

        setSupportActionBar(binding.toolbar)
        textView = findViewById(R.id.id_text_view)
        setClickableText(textView)

        binding.fab.setOnClickListener { view ->

            if(PermissionsUtil.checkIfUserLocationIsGranted(this)){
                getLocation()
            }else{
                ActivityCompat.requestPermissions(this,
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_REQUEST_CODE)
            }
        }
    }

    //This fires the WorkerManager and enqueues my worker
     private fun getLocation(){
        var interval = 1000L * 60 * 60 //One Hour
        if(BuildConfig.DEBUG){
            //Unfortunately we can't set the interval to 1 minute as the WorkManager
            //will automatically set it to the appropriate time when available
            interval = 1000L * 60
        }
         val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
         val request = PeriodicWorkRequestBuilder<LocationWorker>(interval,TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()
         //This was to test if my worker was "working" properly
         //val oneTimeWorkRequest = OneTimeWorkRequest.from(LocationWorker::class.java)
        workManager.enqueue(request)
    }

    //This function is responsible for making a certain text in the textview clickable using Span
    private fun setClickableText(textView: TextView){
        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                Toast.makeText(this@MainActivity,"Success!", Toast.LENGTH_SHORT).show()
            }

            override fun updateDrawState(ds: TextPaint) {
                ds.isUnderlineText = false
                super.updateDrawState(ds)
            }
        }

        val start = textView.text.indexOf("Hello")
        val end = start+"Hello".length
        val spannableString = SpannableString(textView.text)

        spannableString.setSpan(clickableSpan, start, end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.highlightColor = Color.TRANSPARENT
    }

    //This method is overridden so we can state what we want to do if a user grants the app location permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
       if(requestCode == LOCATION_REQUEST_CODE){
           if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               getLocation()
           }else{
               Toast.makeText(
                   applicationContext,
                   "To use the app you must allow background location",
                   Toast.LENGTH_SHORT
               ).show()
           }
       }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}