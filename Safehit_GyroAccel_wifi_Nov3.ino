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

//Firebase Data object
FirebaseData fbdo;
//Firebase Authentication object
FirebaseAuth auth;
//Firebase Configuration object
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0; //latest peice of sent data
//int count = 0;
bool signupOK = false;
/////////////////////////////////////////////////////////////////////////

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

  // Sign up
  if (Firebase.signUp(&config, &auth, "", "")){
    Serial.println("ok");
    signupOK = true;
  }
  else{
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h
  
  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);
  delay(100);
}

void loop() {
	/* Get new sensor events with the readings */
	sensors_event_t a, g, temp; //acceleration, gyro, temperature variables
	mpu.getEvent(&a, &g, &temp);

///////////////////////////new code///////////////////////////////////

if((g.gyro.x>1)&&(g.gyro.y>-1)&&(g.gyro.y<1)){  //front hit 
  sendData = "Hit from front";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
else if((g.gyro.x<-1)&&(g.gyro.y>-1)&&(g.gyro.y<1)){ //back hit 
  sendData = "Hit from back";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}

else if((g.gyro.y>1)&&(g.gyro.x>-1)&&(g.gyro.x<1)){ //left hit 
  sendData = "Hit from left";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
else if((g.gyro.y<-1)&&(g.gyro.x>-1)&&(g.gyro.x<1)){ //right hit 
  sendData = "Hit from right"; 
  Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}

else if((g.gyro.x>1)&&(g.gyro.y>1)){ //front-left hit 
  sendData = "Hit from front-left"; 
  Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
else if((g.gyro.x>1)&&(g.gyro.y<-1)){ //front-right hit 
  sendData = "Hit from front-right";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
else if((g.gyro.x<-1)&&(g.gyro.y>1)){ //back-left hit
  sendData = "Hit from back-left";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
else if((g.gyro.x<-1)&&(g.gyro.y<-1)){ //back-right hit 
  sendData = "Hit from back-right";
   Serial.println.write((const uint8_t*) sendData.c_str(),sendData.length());
}
//////////////////////////end new code///////////////////////////////
*/
if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 1000 || sendDataPrevMillis == 0)){
    sendDataPrevMillis = millis();
  
    // Write Acceleration Values/////////////////////////////////////////////////////////////////////////////
    if (Firebase.RTDB.setFloat(&fbdo, "Gforce_x", (a.acceleration.x)/9.8)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  
  if (Firebase.RTDB.setFloat(&fbdo, "Gforce_y", (a.acceleration.y)/9.8)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  
  if (Firebase.RTDB.setFloat(&fbdo, "Gforce_z", (a.acceleration.z)/9.8)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  
// Write Gyroscope Values/////////////////////////////////////////////////////////////////////////////
if (Firebase.RTDB.setFloat(&fbdo, "x_gyro", g.gyro.x)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  
if (Firebase.RTDB.setFloat(&fbdo, "y_gyro", g.gyro.y)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  
  if (Firebase.RTDB.setFloat(&fbdo, "z_gyro", g.gyro.z)){
      Serial.println("PASSED");
      Serial.println("PATH: " + fbdo.dataPath());
      Serial.println("TYPE: " + fbdo.dataType());
    }
    else {
      Serial.println("FAILED");
      Serial.println("REASON: " + fbdo.errorReason());
    }
  } 
}