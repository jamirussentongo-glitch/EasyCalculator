package com.example.easycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easycalculator.ui.theme.EasyCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyCalculatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalculatorScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen(modifier: Modifier = Modifier) {
    var expression by remember { mutableStateOf("0") }
    var resultText by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    fun evaluateExpression(expr: String): String {
        try {
            val tokens = mutableListOf<String>()
            var number = StringBuilder()
            for (char in expr) {
                if (char.isDigit() || char == '.') {
                    number.append(char)
                } else {
                    if (number.isNotEmpty()) {
                        tokens.add(number.toString())
                        number = StringBuilder()
                    }
                    tokens.add(char.toString())
                }
            }
            if (number.isNotEmpty()) tokens.add(number.toString())

            if (tokens.isEmpty()) return "0"

            val afterMD = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token == "*" || token == "/") {
                    if (afterMD.isEmpty()) return "Error"
                    val prev = afterMD.removeAt(afterMD.size - 1).toDouble()
                    val next = tokens[i + 1].toDouble()
                    val res = if (token == "*") prev * next else prev / next
                    afterMD.add(res.toString())
                    i += 2
                } else {
                    afterMD.add(token)
                    i++
                }
            }

            var finalResult = afterMD[0].toDouble()
            var j = 1
            while (j < afterMD.size) {
                val op = afterMD[j]
                val next = afterMD[j + 1].toDouble()
                finalResult = if (op == "+") finalResult + next else finalResult - next
                j += 2
            }

            return if (finalResult % 1 == 0.0) finalResult.toLong().toString() else finalResult.toString()
        } catch (e: Exception) {
            return "Error"
        }
    }

    fun onButtonClick(label: String) {
        when (label) {
            "AC" -> {
                expression = "0"
                resultText = ""
            }
            "=" -> {
                if (expression != "0" && expression != "Error") {
                    resultText = evaluateExpression(expression)
                }
            }
            "+", "-", "*", "/" -> {
                if (expression == "Error") expression = "0"
                if (resultText.isNotEmpty()) {
                    expression = resultText
                    resultText = ""
                }
                val lastChar = expression.last()
                if (lastChar in "+-*/") {
                    expression = expression.dropLast(1) + label
                } else {
                    expression += label
                }
            }
            else -> { // Numbers
                if (expression == "0" || expression == "Error" || resultText.isNotEmpty()) {
                    expression = label
                    resultText = ""
                } else {
                    expression += label
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Area (The "Screen")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = expression,
                        fontSize = if (resultText.isEmpty()) 44.sp else 24.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        color = if (resultText.isEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (resultText.isNotEmpty()) {
                        Text(
                            text = " = $resultText",
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("AC", "0", "=", "+")
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            buttons.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { label ->
                        Button(
                            onClick = { onButtonClick(label) },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentPadding = PaddingValues(0.dp),
                            colors = when (label) {
                                "/" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                                "*" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary,
                                    contentColor = MaterialTheme.colorScheme.onTertiary
                                )
                                "-" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                "+" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                "=" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                                "AC" -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                                else -> ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(24.dp), // More rounded corners for buttons too
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Text(
                                text = label,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    EasyCalculatorTheme {
        CalculatorScreen()
    }
}
