package com.example.nomorescrolling

import android.os.Bundle
import android.util.Log
import androidx.compose.material3.Switch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nomorescrolling.ui.theme.NoMoreScrollingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NoMoreScrollingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Sébastien",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val image = painterResource(R.drawable.th)
    val circ = painterResource(R.drawable.circular)
    val screenTime = 0
    val preventedTimes = 0
    var isChecked by remember { mutableStateOf(false) } // State for the switch

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState) // Enable vertical scrolling
    ) {
        // Add many items here
        Text(text = "Hello $name")
        Image(
            painter = image,
            contentDescription = "graph",
            modifier = Modifier
                .size(250.dp)
        )

        //buttons
        Row(
            modifier = Modifier
                .fillMaxWidth() // Optional: to fill the width of the parent
                .padding(6.dp), // Add padding around the row
            horizontalArrangement = Arrangement.Start // Align buttons to the start (left)

        ) {
            Button(onClick = { /* Action here */ }) {
                Text("last day")
            }

            Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons

            Button(onClick = { /* Action here */ }) {
                Text("last week")

            }

            Spacer(modifier = Modifier.width(8.dp)) // Add space between buttons

            Button(onClick = { /* Action here */ }) {
                Text("all time")
            }
        }

        Text( text = "Today, you used your phone $screenTime minutes.")
        Text( text = "And we prevented you from scrolling longer $preventedTimes times.")

        Spacer(modifier = Modifier.height(8.dp)) // Add space between buttons

        Text( text = "Most addictive apps :")
        Image(
            painter = circ,
            contentDescription = "graph",
            modifier = Modifier
                .size(250.dp)
        )

        Row {
            Text( text = "Activate AI : ")
            Switch(
                checked = isChecked,
                onCheckedChange = { isChecked = it } // Update state when toggled
            )
        }

        Button(onClick = { /* Action here */Log.d("MyAccessibilityService", "Scroll detected: ") }) {
            Text("Quick phone block")
        }
        Button(onClick = { /* Action here */ }) {
            Text("Set repeated phone block")
        }
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NoMoreScrollingTheme {
        Greeting("Sébastien")
    }
}