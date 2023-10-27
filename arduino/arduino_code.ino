#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>

#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please run `make menuconfig` to and enable it
#endif

BluetoothSerial SerialBT;

Adafruit_MPU6050 mpu;

void setup(void) {
	Serial.begin(115200);

	// Try to initialize!
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

  SerialBT.begin("ESP32test"); //Bluetooth device name
  Serial.println("The device started, now you can pair it with bluetooth!");


	delay(100);
}

void loop() {
	/* Get new sensor events with the readings */
	sensors_event_t a, g, temp;
	mpu.getEvent(&a, &g, &temp);

	/* Print out the values */
  String sendData = "Acceleration X: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.x);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Y: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.y);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Z: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.z);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = " m/s^2 \n";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());



  sendData = "G-Force X: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.x/9.8);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Y: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.y/9.8);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Z: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = float(a.acceleration.z/9.8);
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = " G \n";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());




  sendData = "Rotation X: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = g.gyro.x;
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Y: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = g.gyro.y;
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = ", Z: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = g.gyro.z;
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = " rad/s \n";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());

  sendData = "Temperature: ";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
  sendData = temp.temperature;
  SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
	sendData = " degC \n";
  SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());

  sendData = "\n";
	SerialBT.write((const uint8_t*) sendData.c_str(),sendData.length());
	delay(500);
}