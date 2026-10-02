package com.waste2reward.ai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

private val Green = Color(0xFF1B7F4B)
private val LightGreen = Color(0xFFEAF7EF)
private val Dark = Color(0xFF153326)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Waste2RewardApp() }
    }
}

@Composable
fun Waste2RewardApp() {
    val nav = rememberNavController()
    val vm = remember { AppState() }
    MaterialTheme(colorScheme = lightColorScheme(primary = Green, secondary = Color(0xFF4C8C67))) {
        NavHost(navController = nav, startDestination = "login") {
            composable("login") { Login(nav) }
            composable("home") { Home(nav, vm) }
            composable("scan") { Scan(nav, vm) }
            composable("result") { Result(nav, vm) }
            composable("rewards") { Rewards(nav, vm) }
            composable("impact") { Impact(nav, vm) }
            composable("report") { Report(nav) }
            composable("map") { MapScreen(nav) }
            composable("profile") { Profile(nav, vm) }
        }
    }
}

class AppState {
    var points by mutableIntStateOf(340)
    var greenScore by mutableIntStateOf(87)
    var scans by mutableIntStateOf(34)
    var correct by mutableIntStateOf(21)
    var result by mutableStateOf<ScanResult?>(null)
    var lastWasRescan by mutableStateOf(false)
}

@Composable
fun Shell(title: String, nav: NavHostController, content: @Composable ColumnScope.() -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text(title, fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = { nav.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } })
    }) { p -> Column(Modifier.fillMaxSize().padding(p).padding(20.dp), content = content) }
}

@Composable
fun Login(nav: NavHostController) {
    Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement=Arrangement.Center, horizontalAlignment=Alignment.CenterHorizontally) {
        Text("♻️", fontSize=56.sp)
        Text("Waste2Reward AI", fontSize=30.sp, fontWeight=FontWeight.Bold, color=Dark)
        Text("Scan. Segregate. Recycle. Earn.", color=Green)
        Spacer(Modifier.height(28.dp))
        Button(onClick={nav.navigate("home")}, Modifier.fillMaxWidth().height(56.dp)) { Text("Continue as Guest") }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick={nav.navigate("home")}, Modifier.fillMaxWidth().height(56.dp)) { Text("Email / Password (Demo)") }
        Spacer(Modifier.height(18.dp))
        Text("No account is required for the hackathon demonstration.", fontSize=12.sp)
    }
}

@Composable
fun Home(nav: NavHostController, vm: AppState) {
    Scaffold(bottomBar = { NavigationBar {
        NavigationBarItem(true, { }, { Icon(Icons.Default.Home, null) }, label={Text("Home")})
        NavigationBarItem(false, { nav.navigate("rewards") }, { Icon(Icons.Default.CardGiftcard, null) }, label={Text("Rewards")})
        NavigationBarItem(false, { nav.navigate("impact") }, { Icon(Icons.Default.Eco, null) }, label={Text("Impact")})
        NavigationBarItem(false, { nav.navigate("profile") }, { Icon(Icons.Default.Person, null) }, label={Text("Profile")})
    }}) { p ->
        LazyColumn(Modifier.fillMaxSize().padding(p).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item {
                Text("Waste2Reward AI", fontSize=28.sp, fontWeight=FontWeight.Bold, color=Dark)
                Text("Scan. Segregate. Recycle. Earn.", color=Green)
                Spacer(Modifier.height(8.dp))
                Card(colors=CardDefaults.cardColors(containerColor=LightGreen), shape=RoundedCornerShape(24.dp)) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment=Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Text("${vm.points}", fontSize=32.sp, fontWeight=FontWeight.Bold); Text("Eco Points") }
                        Text("Green Score\n${vm.greenScore}/100", fontWeight=FontWeight.Bold)
                    }
                }
            }
            item {
                Button(onClick={nav.navigate("scan")}, modifier=Modifier.fillMaxWidth().height(62.dp), shape=RoundedCornerShape(18.dp)) {
                    Icon(Icons.Default.CameraAlt, null); Spacer(Modifier.width(10.dp)); Text("SCAN WASTE", fontSize=18.sp, fontWeight=FontWeight.Bold)
                }
            }
            item { Text("Quick actions", fontWeight=FontWeight.Bold, fontSize=19.sp) }
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Quick("Rewards", Icons.Default.CardGiftcard) { nav.navigate("rewards") }
                Quick("Centers", Icons.Default.LocationOn) { nav.navigate("map") }
                Quick("Report", Icons.Default.ReportProblem) { nav.navigate("report") }
            }}
            item { Text("Your progress", fontWeight=FontWeight.Bold, fontSize=19.sp) }
            item { StatCard("Waste scanned", vm.scans.toString(), "Correct segregation", vm.correct.toString()) }
        }
    }
}

@Composable fun Quick(text:String, icon: androidx.compose.ui.graphics.vector.ImageVector, click:()->Unit)=
    OutlinedButton(click, Modifier.weight(1f).height(80.dp), shape=RoundedCornerShape(18.dp)) { Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(icon,null);Text(text)} }

@Composable fun StatCard(a:String,av:String,b:String,bv:String) {
    Card(shape=RoundedCornerShape(20.dp)) { Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement=Arrangement.SpaceEvenly){
        Column(horizontalAlignment=Alignment.CenterHorizontally){Text(av,fontSize=24.sp,fontWeight=FontWeight.Bold);Text(a)}
        Column(horizontalAlignment=Alignment.CenterHorizontally){Text(bv,fontSize=24.sp,fontWeight=FontWeight.Bold);Text(b)}
    }}
}

@Composable
fun Scan(nav: NavHostController, vm: AppState) {
    val context=LocalContext.current
    var mode by remember { mutableStateOf(ScanMode.MIXED) }
    var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
    val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){permission=it}
    val capture=remember { ImageCapture.Builder().build() }
    val scope=rememberCoroutineScope()
    Shell("Smart Waste Scan", nav) {
        Text("Normal visible-light camera analysis", color=Green, fontWeight=FontWeight.Bold)
        Text("This app cannot see through a closed opaque bag. Open the bag and place visible waste in the scan area.")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            ScanMode.entries.forEach { m -> FilterChip(selected=mode==m,onClick={mode=m},label={Text(m.name)}) }
        }
        Spacer(Modifier.height(12.dp))
        if (!permission) {
            Button(onClick={ { launcher.launch(Manifest.permission.CAMERA) } }, Modifier.fillMaxWidth()) { Text("Allow Camera") }
        } else {
            CameraPreview(capture, Modifier.fillMaxWidth().height(430.dp))
            Spacer(Modifier.height(12.dp))
            Button(onClick={
                val file=File(context.cacheDir,"scan_${System.currentTimeMillis()}.jpg")
                capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(), ContextCompat.getMainExecutor(context),
                    object: ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(o: ImageCapture.OutputFileResults) {
                            scope.launch {
                                val items=DemoWasteClassifier(context).classify(Bitmap.createBitmap(16,16,Bitmap.Config.ARGB_8888))
                                val rescan=mode==ScanMode.RESCAN
                                vm.lastWasRescan=rescan
                                val score=SegregationEngine.score(items,rescan)
                                vm.result=ScanResult(items,score,score>=100,SegregationEngine.points(items,score>=100))
                                vm.scans++
                                if(score>=100){vm.correct++;vm.points+=vm.result!!.points;vm.greenScore=(vm.greenScore+1).coerceAtMost(100)}
                                nav.navigate("result")
                            }
                        }
                        override fun onError(e: ImageCaptureException){ }
                    })
            }, Modifier.fillMaxWidth().height(58.dp), shape=RoundedCornerShape(18.dp)) { Icon(Icons.Default.CameraAlt,null);Spacer(Modifier.width(8.dp));Text("Capture & Analyze") }
        }
    }
}

@Composable fun CameraPreview(capture:ImageCapture, modifier:Modifier) {
    val context=LocalContext.current
    val lifecycleOwner=LocalLifecycleOwner.current
    AndroidView(modifier=modifier.clip(RoundedCornerShape(24.dp)), factory={ ctx ->
        val view=PreviewView(ctx)
        val provider=ProcessCameraProvider.getInstance(ctx)
        provider.addListener({
            val p=provider.get()
            val preview=Preview.Builder().build().also{it.setSurfaceProvider(view.surfaceProvider)}
            try { p.unbindAll(); p.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, capture) } catch(_:Exception){}
        }, ContextCompat.getMainExecutor(ctx))
        view
    })
}

@Composable
fun Result(nav:NavHostController, vm:AppState) {
    val r=vm.result ?: return
    Shell("AI Scan Result",nav) {
        Card(colors=CardDefaults.cardColors(containerColor=if(r.properlySeparated)LightGreen else Color(0xFFFFF4E5)),shape=RoundedCornerShape(22.dp)){
            Column(Modifier.padding(20.dp)){Text("${r.score}/100",fontSize=38.sp,fontWeight=FontWeight.Bold);Text(SegregationEngine.label(r.score),fontWeight=FontWeight.Bold);LinearProgressIndicator({r.score/100f},Modifier.fillMaxWidth().padding(vertical=10.dp))}
        }
        Spacer(Modifier.height(14.dp))
        Text("${r.items.size} objects detected",fontSize=20.sp,fontWeight=FontWeight.Bold)
        LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.weight(1f)) {
            items(r.items){item-> Card{Row(Modifier.fillMaxWidth().padding(14.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(item.name,fontWeight=FontWeight.Bold);Text("${item.category.label} • ${(item.confidence*100).toInt()}% confidence");Text("Recommended: ${item.category.disposal}")};Text(if(r.properlySeparated)"+${item.category.points}" else "Separate",fontWeight=FontWeight.Bold,color=Green)}}}
        }
        if(r.properlySeparated) Text("+${r.points} Eco Points",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Green)
        Button(onClick={nav.navigate(if(r.properlySeparated)"rewards" else "scan")},Modifier.fillMaxWidth().height(56.dp)){
            Text(if(r.properlySeparated)"View Rewards" else "Separate & Rescan")
        }
    }
}

@Composable fun Rewards(nav:NavHostController,vm:AppState){
    Shell("Eco Rewards",nav){ Text("My Eco Points",fontSize=18.sp);Text("${vm.points}",fontSize=40.sp,fontWeight=FontWeight.Bold,color=Green);Spacer(Modifier.height(12.dp))
        val rs=listOf(Reward("Plant Sapling",100,"🌱"),Reward("Reusable Cloth Bag",150,"🛍️"),Reward("Reusable Bottle",250,"🥤"),Reward("Eco Product Coupon",500,"♻️"))
        LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.weight(1f)){items(rs){r->Card{Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Text(r.icon,fontSize=30.sp);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(r.name,fontWeight=FontWeight.Bold);Text("${r.points} points • DEMO PROTOTYPE")};Button(onClick={}){Text(if(vm.points>=r.points)"Redeem" else "Locked")}}}}}
        Text("Physical rewards are prototype demonstrations; no automatic real-world reward is guaranteed.",fontSize=12.sp)
    }
}
@Composable fun Impact(nav:NavHostController,vm:AppState)=Shell("Environmental Impact",nav){
    Text("Your measurable activity",fontSize=20.sp,fontWeight=FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    StatCard("Total scans",vm.scans.toString(),"Successful segregations",vm.correct.toString())
    Spacer(Modifier.height(10.dp))
    StatCard("Eco Points earned",vm.points.toString(),"Green Score","${vm.greenScore}/100")
    Spacer(Modifier.height(20.dp));Text("Impact estimates are based on recorded app activity; no exact CO₂ savings are claimed.",fontSize=13.sp)
}
@Composable fun Report(nav:NavHostController)=Shell("Report Waste",nav){
    var sent by remember{mutableStateOf(false)}
    if(sent) {Text("Report submitted",fontSize=24.sp,fontWeight=FontWeight.Bold,color=Green);Text("Prototype report saved locally for demonstration.");}
    else {Text("Report illegal dumping, overflowing garbage, burning, plastic or e-waste.");Spacer(Modifier.height(20.dp));Button(onClick={sent=true},Modifier.fillMaxWidth().height(56.dp)){Text("Submit Demo Report")};Text("A government department is not automatically notified unless an official integration is connected.",fontSize=12.sp)}
}
@Composable fun MapScreen(nav:NavHostController)=Shell("Nearby Recycling Centers",nav){
    Text("Demo locations",fontSize=22.sp,fontWeight=FontWeight.Bold)
    listOf("♻️ Community Recycling Center — Demo location","🔋 E-waste Collection Point — Demo location","🌱 Composting Facility — Demo location").forEach{Card(Modifier.padding(vertical=6.dp)){Text(it,Modifier.padding(18.dp))}}
    Text("Live verified locations require a maps/data integration.",fontSize=12.sp)
}
@Composable fun Profile(nav:NavHostController,vm:AppState)=Shell("Profile",nav){
    Text("Guest User",fontSize=26.sp,fontWeight=FontWeight.Bold);Text("No account required for the hackathon demo.")
    Spacer(Modifier.height(20.dp));StatCard("Eco Points",vm.points.toString(),"Green Score","${vm.greenScore}/100")
    Spacer(Modifier.height(20.dp));Text("Achievements",fontSize=20.sp,fontWeight=FontWeight.Bold)
    listOf("🌱 Beginner Recycler","♻️ Recycling Hero","🌍 Green Champion","🏆 Waste Warrior","🔋 E-Waste Protector","🌳 Planet Protector").forEach{Text(it,Modifier.padding(vertical=8.dp))}
}
