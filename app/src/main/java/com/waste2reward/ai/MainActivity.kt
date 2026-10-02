package com.waste2reward.ai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
        setContent { Waste2RewardApp() }
    }
}

@Composable
fun Waste2RewardApp() {
    var points by remember { mutableIntStateOf(340) }
    var score by remember { mutableIntStateOf(60) }
    MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF2E7D32))) {
        Scaffold { p ->
            LazyColumn(
                modifier = Modifier.padding(p).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Waste2Reward AI", fontSize = 30.sp, color = Color(0xFF1B5E20))
                    Text("Scan. Segregate. Recycle. Earn.")
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Eco Points", fontSize = 18.sp)
                            Text(points.toString(), fontSize = 32.sp)
                            Text("Green Score: " + score + "/100")
                        }
                    }
                }
                item {
                    Text("Smart Waste Scan", fontSize = 22.sp)
                    Text("Use the visible-light phone camera. A normal camera cannot see through an opaque closed bag.")
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { score = 100; points += 50 },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("ANALYZE VISIBLE WASTE") }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Demo AI Result", fontSize = 22.sp)
                            Text("Plastic bottle — 94% — Recycle")
                            Text("Newspaper — 91% — Recycle")
                            Text("Banana peel — 88% — Compost")
                            Text("Aluminium can — 93% — Recycle")
                            Text("Battery — 90% — Authorized e-waste")
                            Spacer(Modifier.height(8.dp))
                            Text("Segregation Score: " + score + "/100")
                            Text("Verified points: " + points)
                        }
                    }
                }
                item { Text("Rewards: Plant Sapling 100 • Cloth Bag 150 • Bottle 250 • Eco Coupon 500") }
                item { Text("Prototype rewards are illustrative unless backed by an actual partner.", color = Color.Gray) }
            }
        }
    }
}
