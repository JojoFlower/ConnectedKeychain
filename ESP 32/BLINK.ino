#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#define uS_TO_S_FACTOR 1000000
#define TIME_TO_SLEEP  2

BLECharacteristic *pCharacteristic;
bool deviceConnected = false;
RTC_DATA_ATTR bool goToSleep = false;

#define SERVICE_UUID        "13ac3d8a-714f-4cf3-adf8-9d9248119dbf"
#define CHARACTERISTIC_UUID "f5423bee-3d9e-4217-a86a-9ac68e51a036"


class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};



void setup() {
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("BLINK");
  // Bluetooth Low-energy IoT Nonlosable Keychain

  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE Characteristic
  pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID,
                      BLECharacteristic::PROPERTY_READ   |
                      BLECharacteristic::PROPERTY_WRITE  |
                      BLECharacteristic::PROPERTY_NOTIFY |
                      BLECharacteristic::PROPERTY_INDICATE
                    );

  // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
  // Create a BLE Descriptor
  pCharacteristic->addDescriptor(new BLE2902());

  // Start the service
  pService->start();

  // Start advertising
  pServer->getAdvertising()->start();
  Serial.println("Waiting a client connection to notify...");
}

void loop() {

  if (!deviceConnected) {
    Serial.println("DEVICE DISCONNECTED");
    goToSleep = !goToSleep;
    if (goToSleep){
        esp_sleep_enable_timer_wakeup(TIME_TO_SLEEP * uS_TO_S_FACTOR);
        Serial.println("Going into deep sleep mode");
        esp_deep_sleep_start();
    }
    else{
      Serial.println("No sleep");
    }
  }
  else{
    Serial.println("DEVICE CONNECTED");
  }
  delay(2000);
}
