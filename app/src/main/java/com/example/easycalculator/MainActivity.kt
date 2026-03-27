package com.example.easycalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            // Simple BODMAS implementation
            // 1. Tokenize (numbers and operators)
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

            // 2. Process Multiplication and Division (*) and (/)
            val afterMD = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token == "*" || token == "/") {
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

            // 3. Process Addition and Subtraction (+) and (-)
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
                if (expression != "0") {
                    resultText = evaluateExpression(expression)
                    // Optionally update expression to result for continuous calculation
                    // expression = resultText 
                }
            }
            "+", "-", "*", "/" -> {
                if (expression == "Error") expression = "0"
                val lastChar = expression.last()
                if (lastChar in "+-*/") {
                    expression = expression.dropLast(1) + label
                } else {
                    expression += label
                }
            }
            else -> { // Numbers
                if (expression == "0" || expression == "Error") {
                    expression = label
                } else {
                    expression += label
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Area - Expression and Result on the same line if possible, or stacked but unified
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
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
                    fontSize = if (resultText.isEmpty()) 48.sp else 24.sp,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    color = if (resultText.isEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
                )
                if (resultText.isNotEmpty()) {
                    Text(
                        text = " = $resultText",
                        fontSize = 48.sp,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        val buttons = listOf(
            listOf("7", "8", "9", "/"),
            listOf("4", "5", "6", "*"),
            listOf("1", "2", "3", "-"),
            listOf("AC", "0", "=", "+")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isOperator = label in listOf("/", "*", "-", "+", "=")
                    val isClear = label == "AC"
                    
                    Button(
                        onClick = { onButtonClick(label) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        colors = when {
                            isOperator -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            isClear -> ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                            else -> ButtonDefaults.buttonColors()
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(text = label, fontSize = 24.sp)
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
