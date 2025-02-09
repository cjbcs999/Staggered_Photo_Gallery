package com.example.staggeredphotogallery

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParser
import kotlin.math.absoluteValue

// Data class to store image information read from the XML file
data class Photo(val fileName: String, val title: String)

// Function to parse res/xml/photos.xml
fun parsePhotosXml(context: Context): List<Photo> {
    val photos = mutableListOf<Photo>()
    val parser = context.resources.getXml(R.xml.photos)
    var eventType = parser.eventType
    while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG && parser.name == "photo") {
            val fileName = parser.getAttributeValue(null, "file") ?: ""
            val title = parser.getAttributeValue(null, "title") ?: ""
            if (fileName.isNotEmpty() && title.isNotEmpty()) {
                photos.add(Photo(fileName, title))
            }
        }
        eventType = parser.next()
    }
    return photos
}

// Single photo item component, enlarges with animation when clicked
@Composable
fun PhotoItem(photo: Photo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Dynamically get image resource ID from drawable based on file name
    val resId = context.resources.getIdentifier(photo.fileName, "drawable", context.packageName)

    // To create different heights, use the hash value of fileName to select a fixed height
    val heights = listOf(150.dp, 200.dp, 250.dp, 180.dp, 220.dp)
    val index = (photo.fileName.hashCode().absoluteValue % heights.size)
    val itemHeight = heights[index]

    // Use Animatable to control scale animation
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .clickable {
                // On click, run coroutine animation: first enlarge, then return to original size
                coroutineScope.launch {
                    scale.animateTo(
                        targetValue = 1.5f,
                        animationSpec = tween(durationMillis = 300)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 300)
                    )
                }
            }
            .padding(4.dp)
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = photo.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = photo.title, style = MaterialTheme.typography.bodyMedium)
    }
}

// Photo gallery screen: Displays images in a waterfall grid using LazyVerticalStaggeredGrid
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhotoGalleryScreen() {
    val context = LocalContext.current
    // Parse XML to get the photo list (use remember to avoid redundant parsing)
    val photos = remember { parsePhotosXml(context) }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(128.dp),
        contentPadding = PaddingValues(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(photos.size) { index ->
            PhotoItem(photo = photos[index])
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Scaffold { paddingValues ->
                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp)) {
                            PhotoGalleryScreen()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoGalleryScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            PhotoGalleryScreen()
        }
    }
}
