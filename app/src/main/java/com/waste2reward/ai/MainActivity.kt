package com.waste2reward.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Green=Color(0xFF238B45)
private val Dark=Color(0xFF124B2A)
private val Mint=Color(0xFFEAF7EE)
private val Gold=Color(0xFFF4B942)
private val Bg=Color(0xFFF7FBF8)

enum class Role(val title:String,val icon:String,val subtitle:String){DONOR("Food Donor","🍽️","Restaurant • Canteen • Hostel"),COLLECTOR("Food Collector","🤝","NGO • Food Bank • Community Kitchen"),PARTNER("Delivery Partner","🛵","Pickup • Delivery • Earnings")}

data class Surplus(val food:String,val qty:String,val status:String,val recipient:String,val fee:String)
data class Waste(val name:String,val kg:String,val route:String)

class MainActivity:ComponentActivity(){
 override fun onCreate(b:Bundle?){super.onCreate(b);setContent{GreenPlateApp()}}
}

@Composable
fun GreenPlateApp(){
 var role by remember{mutableStateOf<Role?>(null)}
 var logged by remember{mutableStateOf(false)}
 var signup by remember{mutableStateOf(false)}
 if(role==null) RolePicker{role=it}
 else if(!logged) AuthScreen(role!!,signup,{signup=!signup},{logged=true})
 else RoleApp(role!!){role=null;logged=false}
}

@Composable
fun RolePicker(onRole:(Role)->Unit){
 Surface(color=Bg,modifier=Modifier.fillMaxSize()){LazyColumn(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
 item{Spacer(Modifier.height(28.dp));Text("🌱",fontSize=58.sp);Text("GREEN PLATE",fontSize=34.sp,fontWeight=FontWeight.ExtraBold,color=Dark);Text("Rescue food. Reward action. Reduce waste.",color=Green,fontSize=16.sp);Spacer(Modifier.height(12.dp));Text("Choose your portal",fontSize=22.sp,fontWeight=FontWeight.Bold,color=Dark)}
 items(Role.values().toList()){r->RoleCard(r){onRole(r)}}
 item{Card(colors=CardDefaults.cardColors(containerColor=Mint)){Column(Modifier.padding(18.dp)){Text("♻️  One connected food-rescue network",fontWeight=FontWeight.Bold,color=Dark);Text("Donors publish surplus → collectors accept → delivery partners collect and deliver → recipients confirm.",color=Color.Gray)}}}
 item{Text("Hackathon demo • Sample data clearly marked",color=Color.Gray,fontSize=12.sp)}
 }}
}

@Composable
fun RoleCard(r: Role, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(r.icon, fontSize = 34.sp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(r.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Dark)
                Text(r.subtitle, color = Color.Gray)
            }
            Text("›", fontSize = 30.sp, color = Green)
        }
    }
}
@Composable
fun AuthScreen(r:Role,signup:Boolean,toggle:()->Unit,login:()->Unit){
 var name by remember{mutableStateOf("")};var email by remember{mutableStateOf("")};var pass by remember{mutableStateOf("")}
 Surface(color=Bg,modifier=Modifier.fillMaxSize()){LazyColumn(Modifier.fillMaxSize().padding(22.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){
 item{Text("←  Back",color=Green,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=8.dp))};item{Text(r.icon,fontSize=42.sp);Text(r.title+" Portal",fontSize=30.sp,fontWeight=FontWeight.Bold,color=Dark);Text(if(signup)"Create your account" else "Welcome back",color=Color.Gray)}
 if(signup)item{OutlinedTextField(name,{name=it},Modifier.fillMaxWidth(),label={Text("Name / Organization")})}
 item{OutlinedTextField(email,{email=it},Modifier.fillMaxWidth(),label={Text("Email or phone")})}
 item{OutlinedTextField(pass,{pass=it},Modifier.fillMaxWidth(),label={Text("Password")})}
 if(signup)item{OutlinedTextField("",{},Modifier.fillMaxWidth(),label={Text("Location")},placeholder={Text("City / service area")})}
 item{Button(onClick=login,modifier=Modifier.fillMaxWidth().height(54.dp)){Text(if(signup)"CREATE ACCOUNT & CONTINUE" else "LOGIN",fontWeight=FontWeight.Bold)}}
 item{TextButton(onClick=toggle,modifier=Modifier.fillMaxWidth()){Text(if(signup)"Already have an account? Login" else "New here? Create an account")}}
 item{Card(colors=CardDefaults.cardColors(containerColor=Mint)){Text("Demo authentication: this hackathon build uses a local demo session. Connect Firebase/Supabase/Auth0 for production authentication.",Modifier.padding(16.dp),color=Dark,fontSize=13.sp)}}
 }}
}

@Composable
fun RoleApp(r:Role,onLogout:()->Unit){
 var page by remember{mutableStateOf(0)}
 val labels=when(r){Role.DONOR->listOf("Home","Surplus","History","Impact","More");Role.COLLECTOR->listOf("Home","Available","Accepted","Impact","More");Role.PARTNER->listOf("Home","Jobs","Earnings","History","More")}
 Scaffold(containerColor=Bg,bottomBar={NavigationBar(containerColor=Color.White){labels.forEachIndexed{i,l->NavigationBarItem(selected=page==i,onClick={page=i},icon={Text(listOf("⌂","＋","✓","★","☰")[i],fontSize=20.sp)},label={Text(l)})}}}){p->
 when(r){
 Role.DONOR->when(page){0->DonorHome(Modifier.padding(p));1->DonorSurplus(Modifier.padding(p));2->HistoryPage("Donation history",Modifier.padding(p));3->ImpactPage("Donor impact",Modifier.padding(p));else->MorePage(r,onLogout,Modifier.padding(p))}
 Role.COLLECTOR->when(page){0->CollectorHome(Modifier.padding(p));1->AvailableFood(Modifier.padding(p));2->AcceptedFood(Modifier.padding(p));3->ImpactPage("Community impact",Modifier.padding(p));else->MorePage(r,onLogout,Modifier.padding(p))}
 Role.PARTNER->when(page){0->PartnerHome(Modifier.padding(p));1->DeliveryJobs(Modifier.padding(p));2->EarningsPage(Modifier.padding(p));3->HistoryPage("Delivery history",Modifier.padding(p));else->MorePage(r,onLogout,Modifier.padding(p))}
 }}
}

@Composable fun Header(title:String,sub:String,m:Modifier){Column(m){Text(title,fontSize=29.sp,fontWeight=FontWeight.ExtraBold,color=Dark);Text(sub,color=Color.Gray)}}
@Composable fun StatCard(title:String,value:String,caption:String,m:Modifier=Modifier){Card(modifier=m,colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Text(title,color=Color.Gray);Text(value,fontSize=27.sp,fontWeight=FontWeight.Bold,color=Green);Text(caption,fontSize=12.sp,color=Color.Gray)}}}
@Composable fun DonorHome(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("Good morning 👋","ABC Canteen • Donor dashboard",Modifier)};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Surplus today","18.5 kg","registered",Modifier.weight(1f));StatCard("Rescued","14.2 kg","this month",Modifier.weight(1f))}};item{Card(colors=CardDefaults.cardColors(containerColor=Dark)){Column(Modifier.padding(19.dp)){Text("🤖 AI waste forecast",color=Color.White);Text("Tomorrow: ~21.8 kg",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Color.White);Text("Suggested: reduce late-service rice batch by 8%.",color=Color.White.copy(alpha=.8f))}}};item{Section("Active rescue");StatusCard("Cooked rice","8 kg","Matched with Community Kitchen","Pickup: Today 1:30 PM")};item{Section("Organic waste");StatusCard("Food scraps","6.2 kg","Biogas route","Scheduled for anaerobic digestion")};item{Text("Edible food is kept in the donation stream only when it passes the safety checklist. Unsafe/ineligible organic material is routed separately.",fontSize=12.sp,color=Color.Gray)}}}
@Composable fun DonorSurplus(m:Modifier){var food by remember{mutableStateOf("")};var qty by remember{mutableStateOf("")};var published by remember{mutableStateOf(false)};LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("Publish surplus","Make safe edible food available to verified collectors.",Modifier)};item{OutlinedTextField(food,{food=it},Modifier.fillMaxWidth(),label={Text("Food name")})};item{OutlinedTextField(qty,{qty=it},Modifier.fillMaxWidth(),label={Text("Quantity (kg)")})};item{Section("Safety checklist")};item{Check("Prepared today / within safe holding time")};item{Check("Covered and hygienically handled")};item{Check("Not previously served or contaminated")};item{Check("Safe temperature maintained")};item{Button(onClick={published=true},modifier=Modifier.fillMaxWidth().height(54.dp)){Text("PUBLISH & FIND COLLECTOR",fontWeight=FontWeight.Bold)}};if(published)item{StatusCard(if(food.isBlank())"Cooked meal" else food,if(qty.isBlank())"5 kg" else qty,"Published • Matching in progress","Collector notification sent")}}}
@Composable fun CollectorHome(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("Collector dashboard","Hope Shelter • verified food collector",Modifier)};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Available","12","donations",Modifier.weight(1f));StatCard("Meals served","428","this month",Modifier.weight(1f))}};item{Section("Today's route");StatusCard("Rice + curry","13 kg","2.4 km away","Delivery partner can be assigned")};item{Section("Safety");Card(colors=CardDefaults.cardColors(containerColor=Mint)){Text("Only accept food that meets the donor safety checklist. Do not accept spoiled, contaminated or unsafe food.",Modifier.padding(16.dp),color=Dark)}};item{Section("Network");StatusCard("3 donors","28.5 kg","matched","8 delivery slots")}}}
@Composable fun AvailableFood(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Header("Available food","Nearby verified surplus",Modifier)};items(listOf("Cooked rice • 8 kg" to "1.2 km","Vegetable curry • 5 kg" to "2.4 km","Chapati • 4 kg" to "0.8 km")){x->Card{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(x.first,fontWeight=FontWeight.Bold);Text(x.second,color=Color.Gray)};Button(onClick={}){Text("ACCEPT")}}}}}}
@Composable fun AcceptedFood(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Header("Accepted donations","Coordinate pickup and delivery.",Modifier)};item{StatusCard("Cooked rice","8 kg","Delivery assigned • Ravi","Pickup 1:30 PM • ETA 2:10 PM")};item{StatusCard("Vegetable curry","5 kg","Ready for pickup","Assign a delivery partner")}}}
@Composable fun PartnerHome(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("Partner dashboard","Ravi • Delivery Partner",Modifier)};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Today's jobs","4","2 completed",Modifier.weight(1f));StatCard("Earnings","₹480","today",Modifier.weight(1f))}};item{Card(colors=CardDefaults.cardColors(containerColor=Dark)){Column(Modifier.padding(19.dp)){Text("Next pickup",color=Color.White);Text("8 kg cooked rice",fontSize=26.sp,fontWeight=FontWeight.Bold,color=Color.White);Text("ABC Canteen → Community Kitchen • 3.2 km",color=Color.White.copy(alpha=.85f));Spacer(Modifier.height(10.dp));Button(onClick={},colors=ButtonDefaults.buttonColors(containerColor=Color.White)){Text("START DELIVERY",color=Dark)}}}};item{Section("Payment");StatusCard("Delivery fee","₹120","Payment pending","Released after recipient confirmation")}}}
@Composable fun DeliveryJobs(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Header("Delivery jobs","Accept, navigate, pickup and confirm delivery.",Modifier)};items(listOf("ABC Canteen → Community Kitchen" to "8 kg • ₹120","Hostel A → Hope Shelter" to "5 kg • ₹100")){x->Card{Column(Modifier.padding(16.dp)){Text(x.first,fontWeight=FontWeight.Bold);Text(x.second,color=Color.Gray);Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button(onClick={}){Text("ACCEPT")};OutlinedButton(onClick={}){Text("MAP")}}}}}}}
@Composable fun EarningsPage(m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("Earnings & payments","Transparent delivery-partner payments.",Modifier)};item{StatCard("Available balance","₹1,280","ready for payout")};item{StatCard("This month","₹4,860","39 completed deliveries")};item{Section("Recent payments")};items(listOf("Delivery #GP1024" to "₹120 • Paid","Delivery #GP1021" to "₹100 • Paid","Delivery #GP1018" to "₹140 • Pending")){x->StatusCard(x.first,x.second,"Delivery fee","Payment status")}}}
@Composable fun HistoryPage(title:String,m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Header(title,"Demo records",Modifier)};items(listOf("GP-1024 • 8 kg • Completed","GP-1021 • 5 kg • Completed","GP-1018 • 4 kg • Completed")){x->Card{Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text(x,Modifier.weight(1f));Text("✓",color=Green,fontWeight=FontWeight.Bold)}}}}}
@Composable fun ImpactPage(title:String,m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header(title,"Measurable outcomes from the Green Plate network.",Modifier)};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Food rescued","146 kg","demo period",Modifier.weight(1f));StatCard("Meals","438","estimated",Modifier.weight(1f))}};item{Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){StatCard("Waste diverted","92 kg","organic route",Modifier.weight(1f));StatCard("CO₂e avoided","≈ 210 kg","estimate",Modifier.weight(1f))}};item{Card(colors=CardDefaults.cardColors(containerColor=Mint)){Column(Modifier.padding(17.dp)){Text("♻️ Biogas pathway",fontWeight=FontWeight.Bold,color=Dark);Text("Inedible organic food is separated from edible donations and sent to an appropriate anaerobic-digestion facility, where organic matter can be converted into biogas.",color=Color.Gray)}}}}}
@Composable fun MorePage(r:Role,logout:()->Unit,m:Modifier){LazyColumn(m.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(13.dp)){item{Header("More","Green Plate settings • undefined",Modifier)};item{Info("🔐","Account & verification","Role-based account, organization profile and verification status.")};item{Info("📍","Service area","Location, pickup radius and delivery coverage.")};item{Info("🔔","Notifications","New surplus, accepted donation, pickup and payment alerts.")};item{Info("♻️","Biogas route","Unsuitable organic material can be routed to appropriate anaerobic digestion facilities.")};item{Info("🤖","AI forecasting","Production, attendance, menu and historical waste data can power a production-ready prediction model.")};item{Button(onClick=logout,modifier=Modifier.fillMaxWidth()){Text("LOG OUT")}};item{Text("Green Plate hackathon demo • Sample data",color=Color.Gray,fontSize=12.sp)}}}
@Composable fun Section(t:String){Text(t,fontSize=20.sp,fontWeight=FontWeight.Bold,color=Dark)}
@Composable fun StatusCard(title:String,value:String,status:String,detail:String){Card(colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp)){Row{Text(title,Modifier.weight(1f),fontSize=18.sp,fontWeight=FontWeight.Bold);Text(value,color=Green,fontWeight=FontWeight.Bold)};Text(status,color=Green,fontWeight=FontWeight.SemiBold);Text(detail,color=Color.Gray,fontSize=13.sp)}}}
@Composable fun Check(t:String){Card{Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text("✓",color=Green,fontWeight=FontWeight.Bold);Spacer(Modifier.width(10.dp));Text(t)}}}
@Composable fun Info(icon:String,title:String,body:String){Card{Column(Modifier.padding(16.dp)){Text(icon,fontSize=24.sp);Text(title,fontWeight=FontWeight.Bold,color=Dark,fontSize=18.sp);Text(body,color=Color.Gray)}}}
