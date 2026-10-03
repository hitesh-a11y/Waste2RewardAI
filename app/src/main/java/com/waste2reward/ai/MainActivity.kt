package com.waste2reward.ai
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Green=Color(0xFF0A7A55)
private val DeepGreen=Color(0xFF064B38)
private val Gold=Color(0xFFFFC107)
private val Bg=Color(0xFFF7FBF8)
data class WasteItem(val name:String,val category:String,val confidence:Int,val action:String,val points:Int)

class MainActivity:ComponentActivity(){
 private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){}
 override fun onCreate(b:Bundle?){super.onCreate(b);if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)permission.launch(Manifest.permission.CAMERA);setContent{Waste2RewardApp()}}
}

@Composable fun Waste2RewardApp(){
 val c=LocalContext.current;val p=remember{c.getSharedPreferences("w2r",Context.MODE_PRIVATE)}
 var tab by remember{mutableIntStateOf(0)};var points by remember{mutableIntStateOf(p.getInt("points",340))}
 var key by remember{mutableStateOf(p.getString("api_key","")?:"")};var results by remember{mutableStateOf<List<WasteItem>>(emptyList())}
 var score by remember{mutableIntStateOf(0)}
 MaterialTheme(colorScheme=lightColorScheme(primary=Green,secondary=Gold,background=Bg)){
  Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Color.White){
   listOf("⌂" to "Home","⌾" to "Scan","★" to "Rewards","☻" to "Profile").forEachIndexed{i,x->NavigationBarItem(selected=tab==i,onClick={tab=i},icon={Text(x.first,fontSize=22.sp)},label={Text(x.second)})}
  }}){pad->when(tab){
   0->Home(points,score,results){tab=1}
   1->Scanner(c,key,{r->results=r;score=r.map{it.confidence}.average().toInt().coerceIn(0,100);points+=r.sumOf{it.points};p.edit().putInt("points",points).apply()},Modifier.padding(pad))
   2->Rewards(points,Modifier.padding(pad))
   else->Profile(key,{key=it;p.edit().putString("api_key",it).apply()},Modifier.padding(pad))
  }}
 }
}

@Composable
fun Home(points:Int, score:Int, results:List<WasteItem>, scan:()->Unit) {
 LazyColumn(
  modifier=Modifier.fillMaxSize().padding(20.dp),
  verticalArrangement=Arrangement.spacedBy(15.dp)
 ) {
  item {
   Row(verticalAlignment=Alignment.CenterVertically) {
    Column(Modifier.weight(1f)) {
     Text("Waste2Reward",fontSize=30.sp,fontWeight=FontWeight.Bold,color=DeepGreen)
     Text("AI",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Green)
     Text("Scan. Segregate. Recycle. Earn.",color=Color.Gray)
    }
    Box(Modifier.size(58.dp).background(Green,CircleShape),contentAlignment=Alignment.Center) {
     Text("♻",color=Color.White,fontSize=31.sp)
    }
   }
  }
  item {
   Card(colors=CardDefaults.cardColors(containerColor=DeepGreen),shape=RoundedCornerShape(22.dp)) {
    Column(Modifier.padding(22.dp)) {
     Text("Your Eco Wallet",color=Color.White)
     Text(points.toString(),fontSize=38.sp,fontWeight=FontWeight.Bold,color=Gold)
     Text("Eco Points",color=Color.White.copy(alpha=.85f))
     Spacer(Modifier.height(12.dp))
     Button(onClick=scan,modifier=Modifier.fillMaxWidth(),colors=ButtonDefaults.buttonColors(containerColor=Gold)) {
      Text("SCAN WASTE",color=Color.Black,fontWeight=FontWeight.Bold)
     }
    }
   }
  }
  item {
   Card {
    Column(Modifier.padding(18.dp)) {
     Text("Segregation Score",fontWeight=FontWeight.Bold,fontSize=18.sp)
     Text(if(score==0) "No scan yet" else score.toString()+" / 100",fontSize=30.sp,color=Green,fontWeight=FontWeight.Bold)
     Text("Analyze visible waste and get disposal guidance.")
    }
   }
  }
  item { Text("How it works",fontSize=21.sp,fontWeight=FontWeight.Bold,color=DeepGreen) }
  item { Step("1","Scan","Point your camera at visible waste.") }
  item { Step("2","Understand","The AI engine identifies likely waste and guidance.") }
  item { Step("3","Earn","Correct segregation adds Eco Points.") }
  item { Text("Latest scan",fontSize=21.sp,fontWeight=FontWeight.Bold,color=DeepGreen) }
  if(results.isEmpty()) item { Text("Nothing scanned yet.",color=Color.Gray) }
  else items(results) { WasteRow(it) }
 }
}

@Composable fun Step(n:String,t:String,b:String){Row(Modifier.fillMaxWidth().background(Color.White,RoundedCornerShape(16.dp)).padding(16.dp),verticalAlignment=Alignment.CenterVertically){Box(Modifier.size(40.dp).background(Green,CircleShape),contentAlignment=Alignment.Center){Text(n,color=Color.White,fontWeight=FontWeight.Bold)};Spacer(Modifier.width(14.dp));Column{Text(t,fontWeight=FontWeight.Bold);Text(b,color=Color.Gray)}}}

@Composable
fun Scanner(c:Context,key:String,done:(List<WasteItem>)->Unit,modifier:Modifier) {
 var capture by remember { mutableStateOf<ImageCapture?>(null) }
 var busy by remember { mutableStateOf(false) }
 var msg by remember { mutableStateOf("Ready to scan visible waste.") }
 var mode by remember { mutableIntStateOf(0) }
 val scope=rememberCoroutineScope()

 Column(modifier.fillMaxSize()) {
  Column(Modifier.padding(18.dp)) {
   Text("Smart Waste Scan",fontSize=28.sp,fontWeight=FontWeight.Bold,color=DeepGreen)
   Text("AI-assisted visible-waste analysis",color=Color.Gray)
   Spacer(Modifier.height(8.dp))
   SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
    listOf("Single","Mixed","Rescan").forEachIndexed { i,s ->
     SegmentedButton(selected=mode==i,onClick={mode=i},shape=SegmentedButtonDefaults.itemShape(i,3)) { Text(s) }
    }
   }
  }
  Box(Modifier.fillMaxWidth().weight(1f).padding(horizontal=16.dp)) {
   if(ContextCompat.checkSelfPermission(c,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) {
    AndroidView(
     factory={ctx->
      val v=PreviewView(ctx)
      val f=ProcessCameraProvider.getInstance(ctx)
      f.addListener({
       val provider=f.get()
       val preview=Preview.Builder().build().also{it.setSurfaceProvider(v.surfaceProvider)}
       val ic=ImageCapture.Builder().build()
       capture=ic
       provider.unbindAll()
       provider.bindToLifecycle(c as ComponentActivity,CameraSelector.DEFAULT_BACK_CAMERA,preview,ic)
      },ContextCompat.getMainExecutor(ctx))
      v
     },
     modifier=Modifier.fillMaxSize()
    )
   } else {
    Text("Camera permission is required.",Modifier.align(Alignment.Center))
   }
  }
  Column(Modifier.padding(16.dp)) {
   Text(msg,color=Color.Gray,fontSize=13.sp)
   Text("A normal phone camera cannot see through a closed opaque bag.",color=Color.Gray,fontSize=12.sp)
   Spacer(Modifier.height(7.dp))
   Button(
    enabled=!busy && capture!=null,
    onClick={
     busy=true
     msg="Capturing and analyzing..."
     val file=File(c.cacheDir,"scan_"+System.currentTimeMillis()+".jpg")
     val out=ImageCapture.OutputFileOptions.Builder(file).build()
     capture?.takePicture(out,ContextCompat.getMainExecutor(c),object:ImageCapture.OnImageSavedCallback {
      override fun onError(e:ImageCaptureException) { busy=false;msg="Capture failed. Try again." }
      override fun onImageSaved(r:ImageCapture.OutputFileResults) {
       scope.launch {
        val x=if(key.isBlank()) {
         msg="Using offline demo intelligence."
         demo(mode)
        } else {
         msg="AI engine is analyzing..."
         OpenAI.analyze(file,key,mode)
        }
        done(x)
        busy=false
        msg="Analysis complete. Open Home for results."
       }
      }
     })
    },
    modifier=Modifier.fillMaxWidth().height(54.dp)
   ) { Text(if(busy)"ANALYZING..." else "CAPTURE & ANALYZE",fontWeight=FontWeight.Bold) }
  }
 }
}

fun demo(mode:Int)=if(mode==0)listOf(WasteItem("Plastic bottle","Recyclable plastic",94,"Empty, rinse and recycle.",10))else listOf(
 WasteItem("Plastic bottle","Recyclable plastic",94,"Empty, rinse and recycle.",10),WasteItem("Newspaper","Paper",91,"Keep dry and send to paper recycling.",10),WasteItem("Banana peel","Organic",88,"Place in compost/organic waste.",10),WasteItem("Aluminium can","Metal",93,"Rinse and recycle.",15),WasteItem("Battery","E-waste / hazardous",90,"Use an authorized collection point.",25))

private object OpenAI {
 private val client=OkHttpClient()
 suspend fun analyze(file:File,key:String,mode:Int):List<WasteItem> = withContext(Dispatchers.IO) {
  try {
   val b64=Base64.encodeToString(file.readBytes(),Base64.NO_WRAP)
   val prompt="You are the private AI engine inside Waste2Reward AI. Analyze only visible waste. Return ONLY a JSON array. Each object: name, category, confidence 0-100, action, points 5-25. Never claim to see through opaque bags."
   val content=JSONArray()
    .put(JSONObject().put("type","input_text").put("text",prompt))
    .put(JSONObject().put("type","input_image").put("image_url","data:image/jpeg;base64,"+b64))
   val input=JSONArray().put(JSONObject().put("role","user").put("content",content))
   val body=JSONObject().put("model","gpt-6-luna").put("input",input).toString().toRequestBody("application/json".toMediaType())
   val req=Request.Builder().url("https://api.openai.com/v1/responses").addHeader("Authorization","Bearer "+key).post(body).build()
   client.newCall(req).execute().use { resp ->
    if(!resp.isSuccessful) return@withContext demo(mode)
    val root=JSONObject(resp.body?.string()?:"")
    var text=root.optString("output_text")
    if(text.isBlank()) {
     val out=root.optJSONArray("output") ?: return@withContext demo(mode)
     val sb=StringBuilder()
     for(i in 0 until out.length()) {
      val cc=out.optJSONObject(i)?.optJSONArray("content") ?: continue
      for(j in 0 until cc.length()) sb.append(cc.optJSONObject(j)?.optString("text")?:"")
     }
     text=sb.toString()
    }
    val clean=text.substringAfter("[").substringBeforeLast("]","")
    if(clean.isBlank()) return@withContext demo(mode)
    val arr=JSONArray("["+clean+"]")
    val list=mutableListOf<WasteItem>()
    for(i in 0 until arr.length()) {
     val q=arr.getJSONObject(i)
     list.add(WasteItem(q.optString("name","Waste"),q.optString("category","Other"),q.optInt("confidence",70).coerceIn(0,100),q.optString("action","Check local guidance."),q.optInt("points",5).coerceIn(5,25)))
    }
    list.ifEmpty{demo(mode)}
   }
  } catch(_:Exception) { demo(mode) }
 }
}

@Composable
fun WasteRow(x:WasteItem) {
 Card {
  Column(Modifier.padding(16.dp)) {
   Row(Modifier.fillMaxWidth()) {
    Text(x.name,Modifier.weight(1f),fontWeight=FontWeight.Bold)
    Text("+"+x.points,color=Green,fontWeight=FontWeight.Bold)
   }
   Text(x.category+" • "+x.confidence+"% confidence",color=Color.Gray)
   Spacer(Modifier.height(4.dp))
   Text(x.action)
  }
 }
}

@Composable fun Rewards(points:Int,modifier:Modifier){val r=listOf("🌱 Plant a sapling" to 100,"👜 Reusable cloth bag" to 150,"♻ Recycling kit" to 250,"🎟 Eco coupon" to 500);LazyColumn(modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Rewards",fontSize=30.sp,fontWeight=FontWeight.Bold,color=DeepGreen);Text("$points Eco Points available")};items(r){(n,cost)->Card{Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(n,fontSize=18.sp,fontWeight=FontWeight.Bold);Text("$cost points")};Button(onClick={}){Text(if(points>=cost)"REDEEM" else "LOCKED")}}}};item{Text("Rewards require real partner integration before representing guaranteed physical benefits.",color=Color.Gray,fontSize=12.sp)}}}

@Composable fun Profile(key:String,save:(String)->Unit,modifier:Modifier){var k by remember(key){mutableStateOf(key)};LazyColumn(modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Profile & Settings",fontSize=30.sp,fontWeight=FontWeight.Bold,color=DeepGreen);Text("Guest mode")};item{Card{Column(Modifier.padding(18.dp)){Text("AI Engine",fontSize=20.sp,fontWeight=FontWeight.Bold);Text("The AI provider runs behind the Waste2Reward interface. No ChatGPT screen is displayed.");Spacer(Modifier.height(10.dp));OutlinedTextField(value=k,onValueChange={k=it},label={Text("OpenAI API key (optional demo)")},singleLine=true);Spacer(Modifier.height(8.dp));Button(onClick={save(k)}){Text("SAVE AI CONNECTION")};Text("Without a key, offline demo intelligence is used. For production, put the secret behind your own secure server.",color=Color.Gray,fontSize=12.sp)}}};item{Step("✓","Privacy","Only images you choose to analyze are sent when an AI connection is configured.")};item{Step("!","Camera limitation","A visible-light phone camera cannot inspect inside an opaque closed bag.")}}}
