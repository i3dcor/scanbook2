package com.i3dcor.scanbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.i3dcor.scanbook.components.HomeSearchBar
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ScanBookTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScanBookApp(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ScanBookApp(modifier: Modifier = Modifier) {
    Column(modifier = modifier
        .fillMaxSize()
        .background(Color.Gray)){
        var myQuery:String by remember { mutableStateOf(    value = "ISBN") }
        HomeSearchBar(
            query = myQuery,
            onQueryChange = {newQuery ->
                myQuery = newQuery
            },
            onSearch = { },
            onMenuClick = {},
            modifier = modifier
        ) { }
    }

}

