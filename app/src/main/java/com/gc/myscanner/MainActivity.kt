package com.gc.myscanner

import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.symbol.emdk.EMDKManager
import com.symbol.emdk.EMDKManager.EMDKListener
import com.symbol.emdk.EMDKManager.FEATURE_TYPE
import com.symbol.emdk.EMDKResults
import com.symbol.emdk.barcode.*
import com.symbol.emdk.barcode.Scanner.DataListener
import com.symbol.emdk.barcode.Scanner.StatusListener
import com.symbol.emdk.barcode.StatusData.ScannerStates


class MainActivity : AppCompatActivity(), EMDKListener, StatusListener, DataListener {
    var dataLength = 0

    // Declare a variable to store EMDKManager object
    var emdkManager: EMDKManager? = null

    // Declare a variable to store Barcode Manager object
    private var barcodeManager: BarcodeManager? = null

    // Declare a variable to hold scanner device to scan
    private var scanner: Scanner? = null

    // Text view to display status of EMDK and Barcode Scanning Operations
    lateinit var statusTextView: TextView

    lateinit var rvBarcodes: RecyclerView

    // Edit Text that is used to display scanned barcode data
    lateinit var dataView: EditText

    private val startRead = false

    lateinit var productScanningAdapter: ProductScanningAdapter
    val productList = ArrayList<ScanningProductsModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }


        statusTextView = findViewById<TextView>(R.id.textViewStatus)
        dataView = findViewById<EditText>(R.id.editText1)
        rvBarcodes = findViewById<RecyclerView>(R.id.rv_products)

        productScanningAdapter = ProductScanningAdapter(this@MainActivity, productList)

        rvBarcodes.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = productScanningAdapter
        }


        val results = EMDKManager.getEMDKManager(applicationContext, this)

        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            updateStatus("EMDKManager object request failed!");
            return;
        } else {
            updateStatus("EMDKManager object initialization is in progress.......");
        }


    }

    private fun initBarcodeManager() {

        barcodeManager = emdkManager!!.getInstance(FEATURE_TYPE.BARCODE) as BarcodeManager

        if (barcodeManager == null) {
            Toast.makeText(this, "Barcode scanning is not supported.", Toast.LENGTH_LONG).show()
            finish()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initScanner() {
        if (scanner == null) {

            // Get default scanner defined on the device
            scanner = barcodeManager!!.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT)
            if (scanner != null) {


                scanner!!.addDataListener(this)


                scanner!!.addStatusListener(this)

                scanner!!.triggerType = Scanner.TriggerType.HARD
                try {

                    scanner!!.enable()
                } catch (e: ScannerException) {
                    updateStatus(e.message)
                    deInitScanner()
                }
            } else {
                updateStatus("Failed to initialize the scanner device.")
            }
        }
    }

    private fun deInitScanner() {
        if (scanner != null) {
            try {
                // Release the scanner
                scanner!!.release()
            } catch (e: java.lang.Exception) {
                updateStatus(e.message)
            }
            scanner = null
        }
    }


    override fun onOpened(emdkManager: EMDKManager?) {


        this.emdkManager = emdkManager;

        initBarcodeManager();

        initScanner();
    }

    override fun onClosed() {

        if (emdkManager != null) {
            emdkManager!!.release();
            emdkManager = null;
        }
        updateStatus("EMDK closed unexpectedly! Please close and restart the application.");
    }

    override fun onStatus(status: StatusData?) {

        val state: ScannerStates = status!!.getState()
        var statusStr = ""



        when (state) {
            ScannerStates.IDLE -> {
                // Scanner is idle and ready to change configuration and submit read.
                statusStr = status.getFriendlyName().toString() + " is enabled and idle..."


                setConfig()
                try {

                    scanner!!.read()
                } catch (e: ScannerException) {
                    updateStatus(e.message)
                }
            }
            ScannerStates.WAITING ->     // Scanner is waiting for trigger press to scan...
                statusStr = "Scanner is waiting for trigger press..."
            ScannerStates.SCANNING ->     // Scanning is in progress...
                statusStr = "Scanning..."
            ScannerStates.DISABLED ->     // Scanner is disabled
                statusStr = status.getFriendlyName().toString() + " is disabled."
            ScannerStates.ERROR ->     // Error has occurred during scanning
                statusStr = "An error has occurred."
            else -> {
            }
        }


        updateStatus(statusStr)
    }

    override fun onData(scanDataCollection: ScanDataCollection?) {

        var dataStr = ""
        if (scanDataCollection != null && scanDataCollection.result === ScannerResults.SUCCESS) {
            val scanData = scanDataCollection.scanData

            for (data in scanData) {

                val barcodeData = data.data

                val labelType = data.labelType

                dataStr = "$barcodeData , $labelType"
            }

// Updates EditText with scanned data and type of label on UI thread.
            updateData(dataStr)
        }

    }


    private fun updateData(result: String) {
        runOnUiThread { // Update the dataView EditText on UI thread with barcode data and its label type
            if (dataLength++ >= 50) {
                // Clear the cache after 50 scans
                dataView.text.clear()
                dataLength = 0
            }

            productList.add(ScanningProductsModel(name = result))

            productScanningAdapter.refresh(productList)
            dataView.append(
                """
    $result
    
    """.trimIndent()
            )
        }
    }


    private fun updateStatus(status: String?) {
        runOnUiThread { // Update the status text view on UI thread with current scanner state
            statusTextView.text = "" + status
        }
    }


    private fun setConfig() {
        if (scanner != null) {
            try {
                // Get scanner config
                val config = scanner!!.config

                // Enable haptic feedback
                if (config.isParamSupported("config.scanParams.decodeHapticFeedback")) {
                    config.scanParams.decodeHapticFeedback = true
                }

                // Set scanner config
                scanner!!.config = config
            } catch (e: ScannerException) {
                updateStatus(e.message!!)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (emdkManager != null) {

// Clean up the objects created by EMDK manager
            emdkManager!!.release()
            emdkManager = null
        }
    }

    override fun onStop() {

        super.onStop()
        try {
            if (scanner != null) {
                // releases the scanner hardware resources for other application
                // to use. You must call this as soon as you're done with the
                // scanning.
                scanner!!.removeDataListener(this)
                scanner!!.removeStatusListener(this)
                scanner!!.disable()
                scanner = null
            }
        } catch (e: ScannerException) {
            e.printStackTrace()
        }
    }

}

