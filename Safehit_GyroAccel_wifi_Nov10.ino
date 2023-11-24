#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
#include <Time.h>

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

#include "addons/TokenHelper.h"  //to generate tokens

#include "addons/RTDBHelper.h"  //allows writing to the realtime database

// Insert your network credentials
#define WIFI_SSID "SM-G920W88950"
#define WIFI_PASSWORD "gwno4163"
//Firebase project API Key
#define API_KEY "AIzaSyBgfi--s3G8vwGUEAKXgQQVkNtkbMSdZ8w"
// Real Time Data Base URL
#define DATABASE_URL "https://safehit-3da2b-default-rtdb.europe-west1.firebasedatabase.app/"

#define USER_EMAIL "steve@gmail.com"  //player/coach credentials (as seen in firebase)
#define USER_PASSWORD "123456"
const char* ntpServer = "pool.ntp.org";
const long gmtOffset_sec = -18000;    //gmt offset of -5:00
const int daylightOffset_sec = 3600;  //1 hour daylight savings offset

FirebaseData fbdo;      //Firebase Data object
FirebaseAuth auth;      //Firebase Authentication object
FirebaseConfig config;  //Firebase Configuration object

String uid;  //user id save variable

unsigned long sendDataPrevMillis = 0;  //latest peice of sent data
unsigned long sendTempPrevMillis = 0;  //for temperature

float Gf_x, Gf_y, Gf_z, Gf_tot;  //G force variables
float GF_thresh = 1.5;           // soft hit threshold
//float GF_conThresh = 4.0;        //hard hit threshold
float gyro_thresh = 0.1;         //gyroscope threshold

String hit_dir;
String macString = " ";


bool light_hit(float Gforce) {
  if (Gforce >= GF_thresh) {
    return true;
  } else {
    return false;
  }
}
/*
bool hard_hit(float Gforce) {
  if ((Gforce > GF_thresh) && (Gforce >= GF_conThresh)) {
    return true;
  } else {
    return false;
  }
}
*/

String savetime() { //function to retrieve current time
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return String("Failed to obtain time");
  }
  char hour[3];
  char min[3];
  char sec[3];

  strftime(hour, 3, "%H", &timeinfo);
  strftime(min, 3, "%M", &timeinfo);
  strftime(sec, 3, "%S", &timeinfo);
  String shour = hour;
  String smin = min;
  String ssec = sec;

  String time = shour + ":" + smin + ":" + ssec; //assign time info in print format

  return time;
}

String monthArr[12] = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };

int convertMonth(String month) { //change month name to month number
  for (int i = 0; i < 12; i++) {
    if (month.equals(monthArr[i])) {
      return i + 1;
    }
  }
}

String savedate() { //function to retrieve current date
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return String("Failed to obtain time");
  }
  char weekDay[10];
  char month[10];
  char year[5];
  char day[3];

  strftime(weekDay, 10, "%A", &timeinfo);
  strftime(month, 10, "%B", &timeinfo);
  strftime(year, 5, "%Y", &timeinfo);
  strftime(day, 3, "%d", &timeinfo);

  String sweekDay = weekDay;
  String smonth = month;
  String syear = year;
  String sday = day;

  int imonth = convertMonth(smonth);

  //String date = sweekDay + " " + sday + " " + smonth + " " + year;
  String date = sday + "/" + imonth + "/" + year; //assign date info in print format
  return date;
}


///////////////////////////////SETUP//////////////////////////////////////////////////////////////

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
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print("_");
    delay(300);
  }

  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);

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

  config.token_status_callback = tokenStatusCallback;  //see addons/TokenHelper.h
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

  Serial.print("ESP Board MAC Address:  ");
  Serial.println(WiFi.macAddress());
  
  macString = WiFi.macAddress();
  
}

void loop() {
  /* Get new sensor events with the readings */
  sensors_event_t a, g, temp;  //acceleration, gyro, temperature variables
  mpu.getEvent(&a, &g, &temp);

  //hit direction checking algorithm
  if ((g.gyro.x > gyro_thresh) && (g.gyro.y > -(gyro_thresh)) && (g.gyro.y < gyro_thresh))  //front hit
    hit_dir = "Hit from front";

  else if ((g.gyro.x < -(gyro_thresh)) && (g.gyro.y > -(gyro_thresh)) && (g.gyro.y < gyro_thresh))  //back hit
    hit_dir = "Hit from back";

  else if ((g.gyro.y > gyro_thresh) && (g.gyro.x > -(gyro_thresh)) && (g.gyro.x < gyro_thresh))  //left hit
    hit_dir = "Hit from left";

  else if ((g.gyro.y < -(gyro_thresh)) && (g.gyro.x > -(gyro_thresh)) && (g.gyro.x < gyro_thresh))  //right hit
    hit_dir = "Hit from right";

  else if ((g.gyro.x > gyro_thresh) && (g.gyro.y > gyro_thresh))  //front-left hit
    hit_dir = "Hit from front-left";

  else if ((g.gyro.x > gyro_thresh) && (g.gyro.y < -(gyro_thresh)))  //front-right hit
    hit_dir = "Hit from front-right";

  else if ((g.gyro.x < -(gyro_thresh)) && (g.gyro.y > gyro_thresh))  //back-left hit
    hit_dir = "Hit from back-left";

  else if ((g.gyro.x < -(gyro_thresh)) && (g.gyro.y < -(gyro_thresh)))  //back-right hit
    hit_dir = "Hit from back-right";

  //writing data to firebase
  if (Firebase.ready() && (millis() - sendDataPrevMillis > 100 || sendDataPrevMillis == 0)) {
    sendDataPrevMillis = millis();

    //Gforce in x,y,and z directions
    Gf_x = a.acceleration.x / 9.8;
    Gf_y = a.acceleration.y / 9.8;
    Gf_z = a.acceleration.z / 9.8;

    //net Gforce of a hit
    Gf_tot = sqrt((Gf_x * Gf_x) + (Gf_y * Gf_y) + (Gf_z * Gf_z));

      if (light_hit(Gf_tot) == true) {
      String time = savetime();
      String date = savedate();
      String hitpath = macString+"/hit";
      const char * hitPath = hitpath.c_str();
      Firebase.RTDB.pushString(&fbdo, hitPath, date + "@" + time + "|" + Gf_tot + "|" + hit_dir);  //print soft hit information
    }
  }

  if (Firebase.ready() && (millis() - sendTempPrevMillis > 60000 || sendTempPrevMillis == 0)) {  //get helmet tempterature every minute
    sendTempPrevMillis = millis();
    String temppath = macString+"/temperature";
    const char * tempPath = temppath.c_str();
    Firebase.RTDB.pushFloat(&fbdo, tempPath, temp.temperature);  //print the helmet temperature
  }
}  //end loop

void printLocalTime() {  //function to print time to serial monitor
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }
  Serial.println(&timeinfo, "%A, %B %d %Y %H:%M:%S");
}
//Mac address 08:D1:F9:A4:F7:38