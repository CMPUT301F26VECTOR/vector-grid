package com.example.project_halfway

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage


@Composable
fun ImagesListUI (toback: ()->Unit) {
    var Images by remember { mutableStateOf<List<ImageAdmin>>(emptyList()) }
    var loading by remember {mutableStateOf(true)}
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        AdminRepository.gettingAllImages(
            onResult = {result->
                Images = result
                loading = false
            },
            onError = {Error->
                message =Error
                loading = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally){
        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Uploaded Images",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        if (loading){Text(text ="Loading....")}
        if (!loading && Images.isEmpty()&& message.isBlank()){
            Text("No Uploaded images found")
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(Images){image->
                Row(modifier = Modifier.fillMaxWidth()
                    .padding(vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ){
                    AsyncImage(
                        model = image.imageURL,
                        contentDescription = "Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(140.dp)
                    )
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Image"
                        )
                    }

            }
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

    Button(
        onClick = toback
    ){
        Text(text ="Go Back")
    }
    }



}