// Joshua Castillo 
// ID: 40047297
// Last modified August 18, 2021

// Sources consulted:
// https://github.com/mobizt/Firebase-ESP8266
// https://github.com/olkal/HX711_ADC
// https://www.youtube.com/watch?v=sxzoAGf1kOo&t=2s&ab_channel=Indrek


#if defined(ESP32)
#include <WiFi.h>
#include <FirebaseESP32.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#include <FirebaseESP8266.h>
#endif

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

//for rounding
#include <math.h> 


#define WIFI_SSID "FIZZ24896"
#define WIFI_PASSWORD "5024castillo"

#define FIREBASE_FCM_SERVER_KEY "AAAAj8l0lgk:APA91bFaSz9jNi-mbhQL8eMGglu8kJLlPKeSQe89RNgCB5bEVUWIoIznp-zArQKrJWCNQJf3uBntJCcj7fieRBeL8bGLigIksbkZg0f2yArGl-Z1jbeyLDHW26zzsOCky-l7WFhHLMjz"

/* 2. Define the API Key */
#define API_KEY "AIzaSyANdq0SfNE1l13n4YX7oNfzdeduT8I9xF4"

/* 3. Define the RTDB URL */
#define DATABASE_URL "delivery-app-390-default-rtdb.firebaseio.com" //<databaseName>.firebaseio.com or <databaseName>.<region>.firebasedatabase.app

/* 4. Define the user Email and password that alreadey registerd or added in your project */
#define USER_EMAIL "delivery.app.390@gmail.com"
#define USER_PASSWORD "Delivery123!"

//Define Firebase Data object
FirebaseData fbdo;

FirebaseAuth auth;
FirebaseConfig config;

unsigned long sendDataPrevMillis = 0;

unsigned long lastTime = 0;

int itemCount = 0;

//Locking
const int lockPin = D15;

//Firebase setup
void connectWiFi();
void connectRTDB();
void connectFCM();

void sendNotification();
void sendOpenDoorNotification();
String getDeviceToken();

//load cell librairies
#include <HX711_ADC.h>
#if defined(ESP8266)|| defined(ESP32) || defined(AVR)
#include <EEPROM.h>
#endif

//pins:
const int HX711_dout = 4; //mcu > HX711 dout pin
const int HX711_sck = 5; //mcu > HX711 sck pin

//HX711 constructor:
HX711_ADC LoadCell(HX711_dout, HX711_sck);

const int calVal_eepromAdress = 0;
unsigned long t = 0;

float oldLoadCellData=0;
float newLoadCellData=0;

void setLoadCellData();
void calibrate();
void sensorsSetUp();

// Lock 
void calibrateLock();
bool lockRequested = false;
void setLockData();
bool getLockData();


bool isItemCountChanged();

void setDoorStatus();
bool getDoorStatus();
bool doorStatus = false;



void setup()
{
    Serial.begin(115200);
    connectWiFi();
    connectRTDB();
    connectFCM();
    sensorsSetUp();
    calibrateLock();
}

void loop()
{
// listen for 
lockChange();

//check if photoelectric sensor is triggered
isItemCountChanged();
}



//=================================
// methods 

void connectWiFi(){
    WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
    Serial.print("Connecting to Wi-Fi");
    while (WiFi.status() != WL_CONNECTED)
    {
        Serial.print(".");
        delay(300);
    }
    Serial.println();
    Serial.print("Connected with IP: ");
    Serial.println(WiFi.localIP());
    Serial.println();
   }
   
void connectRTDB() {
  Serial.printf("Firebase Client v%s\n\n", FIREBASE_CLIENT_VERSION);
  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.database_url = DATABASE_URL;
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h
  Firebase.begin(&config, &auth);
 }


void connectFCM(){
    Firebase.reconnectWiFi(true);
    fbdo.fcm.begin(FIREBASE_FCM_SERVER_KEY);
    Serial.printf("Get device token... %s\n", Firebase.getString(fbdo, "/Device Token/token") ? String(fbdo.stringData()).c_str() : fbdo.errorReason().c_str());
    fbdo.fcm.addDeviceToken(getDeviceToken().c_str());
    fbdo.fcm.setPriority("high");
    fbdo.fcm.setTimeToLive(1000);
  }
  
void sendNotification()
{
  fbdo.fcm.setNotifyMessage("New Parcel!", "You have " + String(itemCount) + " or more parcels in your mailbox");
    fbdo.fcm.setDataMessage("{\"myData\":" + String(itemCount) + "}");
    Serial.printf("Send message... %s\n", Firebase.sendMessage(fbdo, 0) ? "ok" : fbdo.errorReason().c_str());
    if (fbdo.httpCode() == FIREBASE_ERROR_HTTP_CODE_OK)
        Serial.println(fbdo.fcm.getSendResult());
    Serial.println();
}

String getDeviceToken(){
      return (Firebase.getString(fbdo, "/Device Token/token/") ? String(fbdo.stringData()) : fbdo.errorReason());
 }




void setLoadCellData(){
  static boolean newDataReady = 0;
  const int serialPrintInterval = 0; //increase value to slow down serial print activity
  float i = LoadCell.getData();
    // check for new data/start next conversion:
    Serial.println(i);
   if (LoadCell.update()) newDataReady = true;{

        if (newDataReady) {
           oldLoadCellData = roundf(newLoadCellData);
           newLoadCellData = roundf(i);
         
        }
   }
}
 
void calibrate() {
  Serial.println("***");
  Serial.println("Start calibration:");
  Serial.println("Place the load cell an a level stable surface.");
  Serial.println("Remove any load applied to the load cell.");
  Serial.println("Send 't' from serial monitor to set the tare offset.");

  boolean _resume = false;
  while (_resume == false) {
    LoadCell.update();
    if (Serial.available() > 0) {
      if (Serial.available() > 0) {
        char inByte = Serial.read();
        if (inByte == 't') LoadCell.tareNoDelay();
      }
    }
    if (LoadCell.getTareStatus() == true) {
      Serial.println("Tare complete");
      _resume = true;
    }
  }

  Serial.println("Now, place your known mass on the loadcell.");
  Serial.println("Then send the weight of this mass (i.e. 100.0) from serial monitor.");

  float known_mass = 0;
  _resume = false;
  while (_resume == false) {
    LoadCell.update();
    if (Serial.available() > 0) {
      known_mass = Serial.parseFloat();
      if (known_mass != 0) {
        Serial.print("Known mass is: ");
        Serial.println(known_mass);
        _resume = true;
      }
    }
  }

  LoadCell.refreshDataSet(); //refresh the dataset to be sure that the known mass is measured correct
  float newCalibrationValue = LoadCell.getNewCalibration(known_mass); //get the new calibration value

  Serial.print("New calibration value has been set to: ");
  Serial.print(newCalibrationValue);
  Serial.println(", use this as calibration value (calFactor) in your project sketch.");
  Serial.print("Save this value to EEPROM adress ");
  Serial.print(calVal_eepromAdress);
  Serial.println("? y/n");

  _resume = false;
  while (_resume == false) {
    if (Serial.available() > 0) {
      char inByte = Serial.read();
      if (inByte == 'y') {
  #if defined(ESP8266)|| defined(ESP32)
        EEPROM.begin(512);
  #endif
        EEPROM.put(calVal_eepromAdress, newCalibrationValue);
  #if defined(ESP8266)|| defined(ESP32)
        EEPROM.commit();
  #endif
        EEPROM.get(calVal_eepromAdress, newCalibrationValue);
        Serial.print("Value ");
        Serial.print(newCalibrationValue);
        Serial.print(" saved to EEPROM address: ");
        Serial.println(calVal_eepromAdress);
        _resume = true;

      }
      else if (inByte == 'n') {
        Serial.println("Value not saved to EEPROM");
        _resume = true;
      }
    }
  }

  Serial.println("End calibration");
  Serial.println("***");

}

void sensorsSetUp(){
  //===========LOAD CELL SETUP=========//

Serial.println("Starting load cell calibration...");

  LoadCell.begin();
  unsigned long stabilizingtime = 2000; // preciscion right after power-up can be improved by adding a few seconds of stabilizing time
  boolean _tare = true; //set this to false if you don't want tare to be performed in the next step
  LoadCell.start(stabilizingtime, _tare);
  if (LoadCell.getTareTimeoutFlag() || LoadCell.getSignalTimeoutFlag()) {
    Serial.println("Timeout, check MCU>HX711 wiring and pin designations");
    while (1);
  }
  else {
    LoadCell.setCalFactor(1.0); // user set calibration value (float), initial value 1.0 may be used for this sketch
    Serial.println("Startup is complete");
  }
  while (!LoadCell.update());
  calibrate(); //start calibration procedure

  //=========== Trigger sensor SETUP =========//
   pinMode(3,INPUT); //RX wemos d1 r2

  
}

void sendOpenDoorNotification(){
//     optional feature
  }

void setLockData(){
   if ((Firebase.getBool(fbdo, "/LockData/boolean/") ? String(fbdo.boolData()) : fbdo.errorReason()).equalsIgnoreCase("1")){
   
   lockRequested = true;
       } else {
      lockRequested = false;
   }

}

bool getLockData(){
      return lockRequested;
 }

void calibrateLock(){
  //Lock output
  pinMode(lockPin,OUTPUT);
  digitalWrite(lockPin, 0);

  Serial.println("Lock Status is 1:");
  Serial.printf("Get device token...%s\n", Firebase.getBool(fbdo, "/LockData/boolean/") ? String(fbdo.stringData()).c_str() : fbdo.errorReason().c_str());

  //Serial.println(getLockData());
}

void lockChange(){ 
  Serial.println("Lock status is: ");
  Serial.printf("Get device token...%s\n", Firebase.getBool(fbdo, "/LockData/boolean/") ? String(fbdo.boolData()).c_str() : fbdo.errorReason().c_str());
  //Serial.println(getLockData());
    setLockData();
  if(getLockData()){
    digitalWrite(lockPin,1);
  } else {
    digitalWrite(lockPin,0);
  }
}

 bool isDoorOpen(){
  return (!(digitalRead(3)==LOW));
 }


void setDoorStatus(){
  if(isDoorOpen()){
    doorStatus = true;}
    else{
      doorStatus= false;}
  }

 
bool getDoorStatus(){
  return doorStatus;
  }


bool isItemCountChanged(){
  setDoorStatus();
   if(getDoorStatus()){

  delay(5000);
  setLoadCellData();
 if(oldLoadCellData < newLoadCellData) {
  itemCount++;
//  update itemCount
  Serial.printf("Set item count +1 ... %s\n", Firebase.setFloat(fbdo, "/ParcelCount/float", itemCount) ? "ok" : fbdo.errorReason().c_str());
  sendNotification();
  }
  else if(oldLoadCellData > newLoadCellData){
    if(itemCount <= 0){
      itemCount = 0;
    } else {
          itemCount-=1;
    }
//  update itemCount
   Serial.printf("Set item count -1 ... %s\n", Firebase.setFloat(fbdo, "/ParcelCount/float", itemCount) ? "ok" : fbdo.errorReason().c_str());
  }
  else {
//    continue
    }
  }

}
   
