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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.nanoboltron.ui.ParserViewModel
import com.example.nanoboltron.ui.theme.NanoBoltronTheme


class MainActivity : ComponentActivity() {
    private val viewModel: ParserViewModel by viewModels<ParserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var text by remember { mutableStateOf("") }
            var result by remember { mutableStateOf<String?>(null) }
            NanoBoltronTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //AddressInput()
                        result?.let {
                            Text(
                                text = it,
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        TestInput {
                            viewModel.setValue(null, it)
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.loadJsonSchema(this@MainActivity)
                            }
                        ) {
                            Text("Validate")
                        }

                        ElevatedButton(
                            onClick = {
                                viewModel.parseJson(this@MainActivity)
                            }
                        ) {
                            Text("Parse")
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddressInput() {
    var address by remember { mutableStateOf("") }
    TextField(
        value = address,
        onValueChange = { address = it },
        label = { Text("Address") },
        singleLine = false, // Or true, depending on your needs
        modifier = Modifier
            .semantics {
                contentType = ContentType.AddressCountry +
                        ContentType.AddressLocality +
                        ContentType.AddressRegion +
                        ContentType.PostalCode +
                        ContentType.AddressStreet +
                        ContentType.AddressAuxiliaryDetails

            }

    )
}

@Composable
fun TestInput(
    onValueChange: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            onValueChange(it)
        },
        placeholder = {
            Text("Input text here")
        },
        singleLine = false, // Or true, depending on your needs
        modifier = Modifier
            .semantics {
                contentType = ContentType.AddressCountry +
                        ContentType.AddressLocality +
                        ContentType.AddressRegion +
                        ContentType.PostalCode +
                        ContentType.AddressStreet +
                        ContentType.AddressAuxiliaryDetails

            }

    )
}