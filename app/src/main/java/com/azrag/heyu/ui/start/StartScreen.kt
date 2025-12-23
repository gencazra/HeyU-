package com.azrag.heyu.ui.start

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azrag.heyu.ui.theme.HeyUTheme

@Composable
fun StartScreen(
    onLoginClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "heyU!",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Button(
            onClick = onLoginClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("LOG IN")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSignUpClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("SIGN UP")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartScreenPreview() {
    HeyUTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            StartScreen(
                onLoginClicked = {},
                onSignUpClicked = {}
            )
        }
    }
}
