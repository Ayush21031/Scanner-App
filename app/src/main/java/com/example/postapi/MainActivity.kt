package com.example.postapi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.postapi.api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import android.Manifest
import android.app.Application
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.postapi.database.Link
import com.example.postapi.database.LinkViewModel

fun readFromAssets(context: Context, fileName: String): String {
    return try {
        val inputStream = context.assets.open(fileName)
        val size = inputStream.available()
        val buffer = ByteArray(size)
        inputStream.read(buffer)
        inputStream.close()
        String(buffer, Charsets.UTF_8)
    } catch (e: Exception) {
        e.printStackTrace()
        "" // Return empty string on error
    }
}
class MainActivity : ComponentActivity() {

    private val apiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://34.69.236.7:8000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(api::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp(apiService)
        }
    }
}




private fun insertdata(linkview: LinkViewModel, link: Link) {
    linkview.addLink(link)
}

@Composable
fun MyApp(apiService: api, context: Context = LocalContext.current) {
    var name by remember { mutableStateOf(TextFieldValue()) }
    var responseMessage by remember { mutableStateOf("") }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val linkview =remember(context){
        LinkViewModel(context.applicationContext as Application)
    }

    val startForResultCamera = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            bitmap = imageBitmap
        }
    }

    val startForResultGallery = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            val imageBitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            bitmap = imageBitmap
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startForResultCamera.launch(intent)
            Log.d("Permission", "Granted")
        } else {
            showDialog = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Scanner App", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startForResultGallery.launch(intent)
            },
            modifier = Modifier.padding(top = 16.dp).semantics { contentDescription = "select_image_button" }
        ) {
            Text("Select Image")
        }

        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startForResultCamera.launch(intent)
                    }
                    else -> {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp).semantics { contentDescription = "capture_image_button" }
        ) {
            Text("Capture Image")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Permission needed") },
                text = { Text("This permission is needed to take photos.") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        bitmap?.let {
            Image(painter = BitmapPainter(it.asImageBitmap()), contentDescription = "Captured Image", modifier = Modifier.size(200.dp))
        }

        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val imageBase64 = bitmapToBase64(bitmap!!)
                        Log.d("imageBase64", imageBase64)
                        val response = apiService.sendName(NameRequest(imageBase64.toString()))
                        responseMessage = response.message
                        Log.d("responseMessage", response.message)
                        val obj = Link(0,System.currentTimeMillis().toString(),response.message)
                        insertdata(linkview,obj)
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.message))
                        startActivity(context, intent, null)

                    } catch (e: Exception) {
                        responseMessage = "Error: ${e.message}"
                    }
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Send Request")
        }

        Text(
            text = responseMessage,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )
    }

}




fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

