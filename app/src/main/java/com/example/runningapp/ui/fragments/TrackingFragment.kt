package com.example.runningapp.ui.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.runningapp.BuildConfig
import com.example.runningapp.R
import com.example.runningapp.appComponent
import com.example.runningapp.databinding.FragmentTrackingBinding
import com.example.runningapp.models.Run
import com.example.runningapp.service.TrackingService
import com.example.runningapp.service.WholeRunSessionPath
import com.example.runningapp.ui.MainActivity
import com.example.runningapp.ui.viewmodels.MainViewModel
import com.example.runningapp.utilities.Constants.ACTION_FINISH_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_PAUSE_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_RESUME_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.ACTION_START_TRACKING_SERVICE
import com.example.runningapp.utilities.Constants.CANCEL_TRACKING_DIALOG_TAG
import com.example.runningapp.utilities.Constants.MAP_CAMERA_ZOOM
import com.example.runningapp.utilities.Constants.MAP_POLYLINE_COLOR
import com.example.runningapp.utilities.Constants.MAP_POLYLINE_WIDTH
import com.example.runningapp.utilities.Constants.SHARED_PREFERENCES_USER_WEIGHT_KEY
import com.example.runningapp.utilities.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import java.util.*
import javax.inject.Inject
import kotlin.math.round

class TrackingFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentTrackingBinding

    @Inject
    lateinit var viewModel: MainViewModel

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var googleMap: GoogleMap? = null
    private var isTracking = false
    private var runSessionPath: WholeRunSessionPath = mutableListOf()
    private var toolbarMenu: Menu? = null
    private var isStarted = false
    private var currentRunTimeInMillis = 0L
    private val humanWeight: Float
        get() = sharedPref.getFloat(SHARED_PREFERENCES_USER_WEIGHT_KEY, 70f)

    // indicates if past run path is drawn in case activity was destroyed and fragment recreated
    private var pastRunPathRendered = false

    private lateinit var parentActivity: MainActivity

    private val requestLocationPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.entries.any { it.value == false }) {
            if (permissions.entries.any { shouldShowRequestPermissionRationale(it.key) }) {
                showPermissionRationaleDialog()
            } else {
                showPermissionRequiredSettingsLinkDialog()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        Log.d("lifecycle logs", "fragment onCreateView")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)
        Log.d("lifecycle logs", "fragment onViewCreated")

        appComponent.inject(this)

        subscribeToObservers()

        binding.mapView.getMapAsync {
            googleMap = it
            Log.d("tag", "camera move from getMapAsync")
            moveMapCameraToTheLastCoordinatePosition()
            Log.d("tag", "drawAll from getMapAsync")
            drawAllPolylines()
            pastRunPathRendered = true
        }

        requestLocationPermissions()

        binding.btnToggleRun.setOnClickListener {
            toggleRunStatus()
        }
        binding.btnFinishRun.setOnClickListener {
            finishRun()
        }

        parentActivity = requireActivity() as MainActivity

        val menuHost: MenuHost = parentActivity.toolbar
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        if (savedInstanceState != null) {
            val cancelTrackingDialog =
                parentFragmentManager.findFragmentByTag(CANCEL_TRACKING_DIALOG_TAG) as CancelTrackingDialog?
            cancelTrackingDialog?.setPositiveButtonListener {
                cancelRun()
            }
        }
    }

    private fun finishRun() {
        saveRunToDatabase()
        closeCurrentRun()
    }

    private fun showSuccessRunSaveSnackbar() {
        Snackbar.make(
            parentActivity.findViewById(R.id.rootActivityView),
            "Run successfully saved",
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun saveRunToDatabase() {
        val distanceInMeters = TrackingUtility.calculateRunPathDistance(runSessionPath)
        val timeInMillis = currentRunTimeInMillis
        val avgSpeedInKPH =
            round((distanceInMeters / 1000.0) / (timeInMillis / 1000.0 / 60 / 60) * 10) / 10.0
        val dateTimestamp = Calendar.getInstance().timeInMillis
        val burnedCalories = ((distanceInMeters / 1000f) * humanWeight).toInt()

        zoomMapCameraToSeeWholeRunPath()
        googleMap?.snapshot { bitmap ->
            val run = Run(
                distanceInMeters,
                timeInMillis,
                burnedCalories,
                avgSpeedInKPH,
                dateTimestamp,
                image = bitmap
            )
            viewModel.saveRun(run)
            showSuccessRunSaveSnackbar()
        }
    }

    private fun showRunCancelingDialog() {
        CancelTrackingDialog().apply {
            setPositiveButtonListener { cancelRun() }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun cancelRun() {
        closeCurrentRun()
    }

    private fun closeCurrentRun() {
        sendCommandToTrackingService(ACTION_FINISH_TRACKING_SERVICE)
        binding.tvTimer.text = "00:00:00:00"
        findNavController().popBackStack()
    }

    private fun drawAllPolylines() {
        if (runSessionPath.isEmpty() || runSessionPath.last().size < 2) return
        runSessionPath.forEach { solidRun ->
            val polylineOptions = PolylineOptions()
                .color(MAP_POLYLINE_COLOR)
                .width(MAP_POLYLINE_WIDTH)
                .addAll(solidRun)
            googleMap?.addPolyline(polylineOptions)
        }
        Log.d("tag", "all polylines drawed: ${runSessionPath.joinToString(", ")}")
    }

    private fun subscribeToObservers() {
        TrackingService.isStarted.observe(viewLifecycleOwner, Observer {
            isStarted = it
            updateToolbarMenuItemVisibility(it)
        })
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTrackingStatus(it)
        })
        TrackingService.wholeRunSessionPath.observe(viewLifecycleOwner, Observer {
            val isNewFragmentCreated = runSessionPath.isEmpty()
            val forLogsRunSessionPathCopy = runSessionPath
            runSessionPath = it
            Log.d("tag", "camera move from observe")
            moveMapCameraToTheLastCoordinatePosition()
            drawLastCoordinatesPairPolyline()
        })
        TrackingService.runTimeInMillis.observe(viewLifecycleOwner, Observer {
            currentRunTimeInMillis = it
            binding.tvTimer.text =
                if (!isStarted) {
                    "00:00:00:00"
                } else {
                    TrackingUtility.getStopWatchFormatFromMillis(
                        currentRunTimeInMillis,
                        includeMillis = true
                    )
                }
        })
    }

    private fun updateToolbarMenuItemVisibility(isStarted: Boolean) {
        toolbarMenu?.setGroupVisible(0, isStarted)
    }

    private fun moveMapCameraToTheLastCoordinatePosition() {
        if (runSessionPath.isEmpty() || runSessionPath.last().isEmpty()) return
        val cameraUpdate = getDefaultMapCameraZoom()
//        val animated =
            googleMap?.animateCamera(cameraUpdate)
//            ?.also {
//            Log.d("tag", "calling drawAll from animate")
//            if (!pastRunPathRendered) drawAllPolylines()
//            pastRunPathRendered = true
//        }
//        Log.d("tag", "camera $animated")
    }

    private fun getDefaultMapCameraZoom() =
        CameraUpdateFactory.newLatLngZoom(runSessionPath.last().last(), MAP_CAMERA_ZOOM)

    private fun zoomMapCameraToSeeWholeRunPath() {
        if (runSessionPath.isEmpty()) return

        val mapView = binding.mapView
        val bounds = createRunPathBounds()
        val padding = (mapView.height * 0.05f).toInt()

        val cameraUpdate = if (isBoundsBigEnough(bounds)) {
            CameraUpdateFactory.newLatLngBounds(
                bounds,
                mapView.width,
                mapView.height,
                padding
            )
        } else {
            getDefaultMapCameraZoom()
        }
        googleMap?.moveCamera(cameraUpdate)
    }

    private fun createRunPathBounds(): LatLngBounds {
        val boundsBuilder = LatLngBounds.Builder()
        for (lap in runSessionPath) {
            for (pos in lap) {
                boundsBuilder.include(pos)
            }
        }
        return boundsBuilder.build()
    }

    private fun isBoundsBigEnough(bounds: LatLngBounds): Boolean {
        val southwest = bounds.southwest
        val northeast = bounds.northeast
        return southwest.latitude - northeast.latitude > 60 ||
                southwest.longitude - northeast.longitude > 60
    }

    private fun drawLastCoordinatesPairPolyline() {
        if (runSessionPath.isEmpty() || runSessionPath.last().size < 2) return
        val preLastCoordinate = runSessionPath.last()[runSessionPath.last().size - 2]
        val lastCoordinate = runSessionPath.last().last()
        val polylineOptions = PolylineOptions()
            .color(MAP_POLYLINE_COLOR)
            .width(MAP_POLYLINE_WIDTH)
            .add(preLastCoordinate)
            .add(lastCoordinate)
        googleMap?.addPolyline(polylineOptions)
    }

    private fun updateTrackingStatus(isTracking: Boolean) {
        this.isTracking = isTracking
        when {
            !isStarted -> {
                with(binding) {
                    btnToggleRun.text = "Start"
                    btnFinishRun.visibility = View.GONE
                }
            }
            this.isTracking -> {
                with(binding) {
                    btnToggleRun.text = "Stop"
                    btnFinishRun.visibility = View.GONE
                }
            }
            else -> {
                with(binding) {
                    btnToggleRun.text = "Resume"
                    btnFinishRun.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun toggleRunStatus() {
        if (!isTracking) {
            if (!isStarted) {
                sendCommandToTrackingService(ACTION_START_TRACKING_SERVICE)
            } else {
                sendCommandToTrackingService(ACTION_RESUME_TRACKING_SERVICE)
            }
        } else {
            sendCommandToTrackingService(ACTION_PAUSE_TRACKING_SERVICE)
        }
    }

    private fun sendCommandToTrackingService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("You need to accept this permission to track your running path.")
            .setPositiveButton("Ok") { _, _ ->
                requestLocationPermissionsLauncher.launch(TrackingUtility.LOCATION_PERMISSIONS)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showPermissionRequiredSettingsLinkDialog() {
        AlertDialog.Builder(requireContext())
            .setMessage("You need to accept this permission from the settigs.")
            .setPositiveButton("Ok") { _, _ ->
                val appDetailsSettingsIntent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                )
                startActivity(appDetailsSettingsIntent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun requestLocationPermissions() {
        when {
            TrackingUtility.hasAllLocationPermissions(requireContext()) -> {
                return
            }
            TrackingUtility.LOCATION_PERMISSIONS.any {
                shouldShowRequestPermissionRationale(it)
            } -> {
                showPermissionRationaleDialog()
            }
            else -> requestLocationPermissionsLauncher.launch(TrackingUtility.LOCATION_PERMISSIONS)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
        drawAllPolylines()
        Log.d("lifecycle logs", "fragment onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("lifecycle logs", "fragment onResume")
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        Log.d("lifecycle logs", "fragment onPause")
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
        Log.d("lifecycle logs", "fragment onStop")
    }

    override fun onDestroy() {
        binding.mapView.onDestroy()
        super.onDestroy()
        Log.d("lifecycle logs", "fragment onDestroy")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
        Log.d("lifecycle logs", "fragment onSavedInstanceState")
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.tracking_fragment_toolbar_menu, menu)
        toolbarMenu = menu
        updateToolbarMenuItemVisibility(isStarted)
        Log.d("lifecycle logs", "onCreateMenu")
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.miCancelRun -> showRunCancelingDialog()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("lifecycle logs", "fragment onCreate")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("lifecycle logs", "fragment onAttach")
    }

    override fun onDetach() {
        super.onDetach()

        Log.d("lifecycle logs", "fragment onDetach")
    }

    override fun onDestroyView() {

        super.onDestroyView()
        Log.d("lifecycle logs", "fragment onDestroyView")
    }
}