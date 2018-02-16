package com.sentiance.android.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.text.InputType
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.sentiance.android.R
import com.sentiance.android.service.LocationService
import com.sentiance.android.ui.base.BaseActivity

private const val PERMISSION_REQUEST_LOCATION = 1000

class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap

    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Check if location permission is granted.
        // If not then request for the permission
        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            mapAsync()
        } else {
            requestPermissionsSafely(permissions, PERMISSION_REQUEST_LOCATION)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }

        // Attach a map click listener
        mMap.setOnMapClickListener(this)
    }

    private fun mapAsync() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Ask user to enter the geo fence radius
    // start the service and keep checking the location in background
    override fun onMapClick(latLng: LatLng?) {
        MaterialDialog.Builder(this)
                .title(R.string.enter_radius)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .input("", "", MaterialDialog.InputCallback { _, input ->
                    val intent = Intent(this, LocationService::class.java)
                    intent.putExtra("latitude", latLng!!.latitude)
                    intent.putExtra("longitude", latLng.longitude)
                    intent.putExtra("radius", input.toString().trim()) // in meters
                    startService(intent)
                }).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            // Request for location permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                mapAsync()
            } else {
                // Permission request was denied.
                Toast.makeText(this, getString(R.string.permission_location_denied), Toast.LENGTH_LONG).show()
            }
        }
    }

}
