package com.example.nanoboltron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.nanoboltron.ui.ParserViewModel
import com.example.nanoboltron.ui.theme.NanoBoltronTheme


class MainActivity : ComponentActivity() {
    private val viewModel: ParserViewModel by viewModels<ParserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NanoBoltronTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ElevatedButton(
                            onClick = {
                                viewModel.loadJsonSchema(application)
                            }
                        ) {
                            Text("Load json schema")
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.loadData(application)
                            }
                        ) {
                            Text("Load data")
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.nodeLookUp(application)
                            }
                        ) {
                            Text("Look up node")
                        }
                    }
                }
            }
        }
    }
}
