#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>

#include <Arduino.h>
#if defined(ESP32)
  #include <WiFi.h>
#elif defined(ESP8266)
  #include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>

Adafruit_MPU6050 mpu;
//////////////////////////WIFI AND DB STUFF///////////////////////////////////////////
#include <Arduino.h>
#if defined(ESP32)
  #include <WiFi.h>
#elif defined(ESP8266)
  #include <ESP8266WiFi.h>
#endif
#include <Firebase_ESP_Client.h>

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "SM-G920W88950"
#define WIFI_PASSWORD "gwno4163"
//Firebase project API Key
#define API_KEY "AIzaSyBgfi--s3G8vwGUEAKXgQQVkNtkbMSdZ8w"
// Real Time Data Base URL
#define DATABASE_URL "https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/" 

#define USER_EMAIL "steve@gmail.com"
#define USER_PASSWORD "123456"

FirebaseData fbdo; //Firebase Data object
FirebaseAuth auth; //Firebase Authentication object
FirebaseConfig config; //Firebase Configuration object

String uid; //user id save variable

unsigned long sendDataPrevMillis = 0; //latest peice of sent data
unsigned long sendTempPrevMillis =0; //for temperature 

float Gf_x, Gf_y, Gf_z,Gf_tot; //G force variables
float GF_thresh = 2.0; // soft hit threshold
float GF_conThresh = 4.0; //hard hit threshold
float gyro_thresh = 0.1; //gyroscope threshold

bool light_hit(float Gforce){
  if((Gforce >= GF_thresh)&&(Gforce < GF_conThresh)){
    return true;
  }
  else {
    return false;
  }
} 

bool hard_hit(float Gforce){
  if((Gforce > GF_thresh)&&(Gforce >= GF_conThresh)){
    return true;
  }
  else {
    return false;
  }
}


void setup(void) {
  //setting baud rate
	Serial.begin(115200);

	// Try to initialize MPU
	if (!mpu.begin()) {
		Serial.println("Failed to find MPU6050 chip");
		while (1) {
		  delay(10);
		}
	}
	Serial.println("MPU6050 Found!");

	// set accelerometer range to +-8G
	mpu.setAccelerometerRange(MPU6050_RANGE_8_G);

	// set gyro range to +- 500 deg/s
	mpu.setGyroRange(MPU6050_RANGE_500_DEG);

	// set filter bandwidth to 21 Hz
	mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

//connection to wifi
WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED){
    Serial.print("_");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();

  // Assign the api key (required) 
  config.api_key = API_KEY;
  // Assign the Real Time DataBase URL (required)
  config.database_url = DATABASE_URL;

  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;

  /* Assign the callback function for the long running token generation task */
    
  Firebase.reconnectWiFi(true);
  fbdo.setResponseSize(4096);

  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h
   // Assign the maximum retry of token generation
  config.max_token_generation_retry = 5;

  Firebase.begin(&config, &auth);
  // Getting the user UID might take a few seconds
  Serial.println("Getting User UID");
  while ((auth.token.uid) == "") {
    Serial.print('.');
    delay(1000);
  }

  // Print user UID
  uid = auth.token.uid.c_str();
  Serial.print("User UID: ");
  Serial.println(uid);
}

void loop() {
	/* Get new sensor events with the readings */
	sensors_event_t a, g, temp; //acceleration, gyro, temperature variables
	mpu.getEvent(&a, &g, &temp);

///////////////////////////new code///////////////////////////////////

if((g.gyro.x>gyro_thresh)&&(g.gyro.y>-(gyro_thresh))&&(g.gyro.y<gyro_thresh)){  //front hit 
   Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from front");
}
else if((g.gyro.x<-(gyro_thresh))&&(g.gyro.y>-(gyro_thresh))&&(g.gyro.y<gyro_thresh)){ //back hit 
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from back");
}

else if((g.gyro.y>gyro_thresh)&&(g.gyro.x>-(gyro_thresh))&&(g.gyro.x<gyro_thresh)){ //left hit 
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from left");
}
else if((g.gyro.y<-(gyro_thresh))&&(g.gyro.x>-(gyro_thresh))&&(g.gyro.x<gyro_thresh)){ //right hit 
 Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from right");
}
else if((g.gyro.x>gyro_thresh)&&(g.gyro.y>gyro_thresh)){ //front-left hit 
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from front-left");
}
else if((g.gyro.x>gyro_thresh)&&(g.gyro.y<-(gyro_thresh))){ //front-right hit 
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from front-right");
}
else if((g.gyro.x<-(gyro_thresh))&&(g.gyro.y>gyro_thresh)){ //back-left hit
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from back-left");
}
else if((g.gyro.x<-(gyro_thresh))&&(g.gyro.y<-(gyro_thresh))){ //back-right hit 
  Firebase.RTDB.pushString(&fbdo, "Time/Direction", "Hit from back-right");
}
//////////////////////////end new code///////////////////////////////



if (Firebase.ready() && (millis() - sendDataPrevMillis > 100 || sendDataPrevMillis == 0)){
    sendDataPrevMillis = millis();
  
  // Write Acceleration Values/////////////////////////////////////////////////////////////////////////////

  Gf_x = a.acceleration.x/9.8;
  Gf_y = a.acceleration.y/9.8;
  Gf_z = a.acceleration.z/9.8;

  Gf_tot = sqrt((Gf_x*Gf_x)+(Gf_y*Gf_y)+(Gf_z*Gf_z));

  if(light_hit(Gf_tot)==true){ 
    Firebase.RTDB.pushFloat(&fbdo, "Time/hit/Soft hit", Gf_tot); //if a significant enough light impact is detected, g_force value will show
  }
  if(hard_hit(Gf_tot)==true){ 
    Firebase.RTDB.pushFloat(&fbdo, "Time/hit/Hard hit", Gf_tot); //if a significant enough light impact is detected, g_force value will show
  }  
  
 }

 if (Firebase.ready() && (millis() - sendTempPrevMillis > 300000 || sendTempPrevMillis == 0)){
    sendTempPrevMillis = millis();
    Firebase.RTDB.pushFloat(&fbdo, "helmet/temperature", temp.temperature);
} 
}   

// Write Gyroscope Values/////////////////////////////////////////////////////////////////////////////
    //Firebase.RTDB.pushFloat(&fbdo, "x_gyro", g.gyro.x);
    //Firebase.RTDB.pushFloat(&fbdo, "y_gyro", g.gyro.y);
    //Firebase.RTDB.pushFloat(&fbdo, "z_gyro", g.gyro.z);
    //Serial.println("x_gyro: ", g.gyro.x);
    //Serial.println("y_gyro: ", g.gyro.y);
    //Serial.println("z_gyro: ", g.gyro.z);
    //Firebase.RTDB.pushFloat(&fbdo, "Gforce_x", Gf_x);
    //Firebase.RTDB.pushFloat(&fbdo, "Gforce_y", Gf_y);
    //Firebase.RTDB.pushFloat(&fbdo, "Gforce_z", Gf_z);
   