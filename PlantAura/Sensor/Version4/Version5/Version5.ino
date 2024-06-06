#include <WiFi.h>
#include <HTTPClient.h>
#include <WebServer.h>
#include <DNSServer.h>
#include <WiFiManager.h>
#include <ESPmDNS.h>
#include <ArduinoJson.h>
#include <time.h>
#include <DHT.h>
#include <ModbusMaster.h>

// Definir pines para RE y DE del módulo RS485
#define RE_DE 4

// Instancia de ModbusMaster
ModbusMaster sensor;

void preTransmission() {
  digitalWrite(RE_DE, 1);
}

void postTransmission() {
  digitalWrite(RE_DE, 0);
}

// Pines y configuración de sensores
#define DHTPIN 14       // Pin donde está conectado el DHT11
#define DHTTYPE DHT11   // Definición del tipo de sensor DHT
DHT dht(DHTPIN, DHTTYPE);
#define SENSOR_LUZ_PIN 34  // Pin analógico para el sensor de luz

// Variables para almacenar las mediciones del sensor Modbus
float humedadModbus, temperaturaSuelo, ph;
int conductividad, nitrogeno, fosforo, potasio, salinidad, tds;

// Variables para almacenar las mediciones del DHT11 y el sensor de luz
float temperatura, humedadAmbiente, luminosidad;

// Reemplaza con tus credenciales de Firebase
#define FIREBASE_PROJECT_ID "plantaura2"

// Dividir el access token en partes
const char REFRESH_TOKEN_PART1[] PROGMEM = "AMf-vBz1SHrujat2MMLvTDXEdgsnZyPNzI7DX_RRne1HCC-HiPLvgZisv_VpjLFQh2uam8qw247xiAgPYFSIugpVI6emBzIGDHqfLfvmGPreUTEUCv7dKQuEU99m-RgnA9c";
const char REFRESH_TOKEN_PART2[] PROGMEM = "X49UmIO-R6sQIZP_juBw_9efcl7IuzM289fGxSLHKFsOAjE6gs";
const char REFRESH_TOKEN_PART3[] PROGMEM = "jTH_I-5wKY8g6BzXEEKaG";
const char REFRESH_TOKEN_PART4[] PROGMEM = "tvZrd0XnDYY";
const char REFRESH_TOKEN_PART5[] PROGMEM = "k-UKwI7VIYNeA";

// API key de Firebase
#define API_KEY "AIzaSyANGl-rSnDDis83bI0Wkf-5sc6_pua7NMQ"

// Access token y su tiempo de expiración
String accessToken;
unsigned long tokenExpirationTime;

// Definir objetos globales
WebServer server(80);

void handleRoot();
void handleNotFound();
void handleData();
void saveConfigCallback();

bool shouldSaveConfig = false;
unsigned long lastSendTime = 0;
const unsigned long sendInterval = 60000; // 10 minutos en milisegundos

void setup() {
  Serial.begin(115200);
  
  // Inicializar Modbus
  Serial1.begin(4800, SERIAL_8N1, 16, 17); // Ajusta los pines TX=17 y RX=16 según tu conexión
  pinMode(RE_DE, OUTPUT);
  digitalWrite(RE_DE, 0);

  sensor.begin(1, Serial1);  // 1 es la dirección del esclavo
  sensor.preTransmission(preTransmission);
  sensor.postTransmission(postTransmission);

  // Inicializar sensores
  dht.begin();
  pinMode(SENSOR_LUZ_PIN, INPUT);

  WiFiManager wifiManager;
  wifiManager.setSaveConfigCallback(saveConfigCallback);

  if (!wifiManager.autoConnect("ESP32_AP_Config")) {
    Serial.println("No se pudo conectar y no se proporcionaron credenciales.");
    delay(3000);
    ESP.restart();
  }

  Serial.println("Conectado a la red WiFi.");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());
  Serial.print("RSSI: ");
  Serial.println(WiFi.RSSI());

  if (!MDNS.begin("esp32")) {
    Serial.println("Error al iniciar mDNS");
    return;
  }
  Serial.println("mDNS iniciado");

  MDNS.addService("http", "tcp", 80);

  server.on("/", handleRoot);
  server.on("/data", handleData);
  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("Servidor HTTP iniciado");

  // Configurar NTP
  configTime(0, 0, "pool.ntp.org", "time.nist.gov");
  Serial.println("Esperando sincronización de tiempo...");
  while (!time(nullptr)) {
    Serial.print(".");
    delay(1000);
  }
  Serial.println("\nSincronización de tiempo completada");

  // Obtener el primer access token
  refreshAccessToken();
}

void loop() {
  server.handleClient();

  if (shouldSaveConfig) {
    Serial.println("Las credenciales WiFi fueron guardadas.");
    shouldSaveConfig = false;
  }

  // Leer datos del sensor Modbus
  uint8_t result = sensor.readHoldingRegisters(0x0000, 9);
  if (result == sensor.ku8MBSuccess) {
    humedadModbus = sensor.getResponseBuffer(0) / 10.0;
    temperaturaSuelo = sensor.getResponseBuffer(1) / 10.0;
    conductividad = sensor.getResponseBuffer(2);
    ph = sensor.getResponseBuffer(3) / 10.0;
    nitrogeno = sensor.getResponseBuffer(4);
    fosforo = sensor.getResponseBuffer(5);
    potasio = sensor.getResponseBuffer(6);
    salinidad = sensor.getResponseBuffer(7);
    tds = sensor.getResponseBuffer(8);
  } else {
    Serial.print("Error en la comunicación Modbus. Código: ");
    Serial.println(result);
  }

  // Leer datos del sensor DHT11 y el sensor de luz
  temperatura = dht.readTemperature();
  humedadAmbiente = dht.readHumidity();

  int sensorLuzRaw = analogRead(SENSOR_LUZ_PIN);
  sensorLuzRaw = 4095 - sensorLuzRaw; // Invertir los valores del sensor de luz
  float voltajeLuz = (sensorLuzRaw / 4095.0) * 3.3;  // Convertir el valor ADC a voltaje
  luminosidad = (voltajeLuz / 1.0) * 100;  // Asumir que 3.3V = 100%, ajustar según el sensor

  unsigned long currentTime = millis();
  if (currentTime - lastSendTime >= sendInterval) {
    Serial.println("Enviando datos a Firestore...");
    if (millis() > tokenExpirationTime) {
      refreshAccessToken();
    }
    sendDataToFirestore();
    lastSendTime = currentTime;
  }
}

void handleRoot() {
  Serial.println("Petición recibida en la ruta raíz");
  server.send(200, "text/plain", "ESP32 conectado correctamente.");
}

void handleData() {
  Serial.println("Petición recibida en la ruta /data");

  // Crear el objeto JSON con el ID y el nombre del sensor
  DynamicJsonDocument doc(1024);
  doc["id"] = getDeviceID();
  doc["sensor"] = "Sensor PlantAura Pro";

  String jsonResponse;
  serializeJson(doc, jsonResponse);

  // Enviar la respuesta JSON
  server.send(200, "application/json", jsonResponse);
}

void handleNotFound() {
  Serial.println("Petición no encontrada");
  String message = "File Not Found\n\n";
  message += "URI: ";
  message += server.uri();
  message += "\nMethod: ";
  message += (server.method() == HTTP_GET) ? "GET" : "POST";
  message += "\nArguments: ";
  message += server.args();
  message += "\n";

  for (uint8_t i = 0; i < server.args(); i++) {
    message += " " + server.argName(i) + ": " + server.arg(i) + "\n";
  }

  server.send(404, "text/plain", message);
}

void saveConfigCallback() {
  Serial.println("Se guardó la configuración.");
  shouldSaveConfig = true;
}

String getDeviceID() {
  return WiFi.macAddress();
}

String getFormattedTime() {
  time_t now;
  struct tm timeinfo;
  time(&now);
  localtime_r(&now, &timeinfo);
  char buffer[25];
  strftime(buffer, sizeof(buffer), "%Y-%m-%d %H:%M:%S", &timeinfo);
  return String(buffer);
}

String getRefreshToken() {
  String refreshToken = String(REFRESH_TOKEN_PART1) + String(REFRESH_TOKEN_PART2) +
                        String(REFRESH_TOKEN_PART3) + String(REFRESH_TOKEN_PART4) +
                        String(REFRESH_TOKEN_PART5);
  return refreshToken;
}

void refreshAccessToken() {
  HTTPClient http;
  http.begin("https://securetoken.googleapis.com/v1/token?key=" + String(API_KEY));
  Serial.println("https://securetoken.googleapis.com/v1/token?key=" + String(API_KEY));
  http.addHeader("Content-Type", "application/x-www-form-urlencoded");

  String postData = "grant_type=refresh_token&refresh_token=" + getRefreshToken();
  int httpResponseCode = http.POST(postData);

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("HTTP Response code: " + String(httpResponseCode));
    Serial.println("Response: " + response);

    DynamicJsonDocument doc(1024);
    deserializeJson(doc, response);
    accessToken = doc["access_token"].as<String>();
    int expiresIn = doc["expires_in"].as<int>();
    tokenExpirationTime = millis() + (expiresIn * 1000);
  } else {
    Serial.print("Error en la solicitud HTTP. Código: ");
    Serial.println(httpResponseCode);
    Serial.print("Error: ");
    Serial.println(http.errorToString(httpResponseCode).c_str());
  }

  http.end();
}

void sendDataToFirestore() {
  HTTPClient http;
  String url = "https://firestore.googleapis.com/v1/projects/" + String(FIREBASE_PROJECT_ID) + "/databases/(default)/documents/Plantas/" + getDeviceID() + "/datos";

  http.begin(url);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Authorization", "Bearer " + accessToken);

  // Verificar si las lecturas son válidas
  if (isnan(temperatura) || isnan(humedadAmbiente) || isnan(luminosidad)) {
    Serial.println("Error al leer los datos de los sensores");
    return;
  }

  String timestamp = getFormattedTime();

  DynamicJsonDocument doc(1024);
  JsonObject fields = doc.createNestedObject("fields");
  JsonObject timestampObj = fields.createNestedObject("timestamp");
  timestampObj["stringValue"] = timestamp;
  JsonObject temperaturaObj = fields.createNestedObject("temperatura");
  temperaturaObj["doubleValue"] = round(temperatura * 100.0) / 100.0;
  JsonObject temperaturaSueloObj = fields.createNestedObject("temperaturaSuelo");
  temperaturaSueloObj["doubleValue"] = round(temperaturaSuelo * 100.0) / 100.0;
  JsonObject humedadAmbienteObj = fields.createNestedObject("humedadAmbiente");
  humedadAmbienteObj["doubleValue"] = round(humedadAmbiente * 100.0) / 100.0;
  JsonObject humedadSueloObj = fields.createNestedObject("humedadSuelo");
  humedadSueloObj["doubleValue"] = round(humedadModbus * 100.0) / 100.0;
  JsonObject luminosidadObj = fields.createNestedObject("luminosidad");
  luminosidadObj["doubleValue"] = round(luminosidad * 100.0) / 100.0;
  JsonObject conductividadObj = fields.createNestedObject("conductividad");
  conductividadObj["doubleValue"] = conductividad;
  JsonObject phObj = fields.createNestedObject("ph");
  phObj["doubleValue"] = round(ph * 100.0) / 100.0;
  JsonObject nitrogenoObj = fields.createNestedObject("nitrogeno");
  nitrogenoObj["doubleValue"] = nitrogeno;
  JsonObject fosforoObj = fields.createNestedObject("fosforo");
  fosforoObj["doubleValue"] = fosforo;
  JsonObject potasioObj = fields.createNestedObject("potasio");
  potasioObj["doubleValue"] = potasio;
  JsonObject salinidadObj = fields.createNestedObject("salinidad");
  salinidadObj["doubleValue"] = salinidad;
  JsonObject tdsObj = fields.createNestedObject("tds");
  tdsObj["doubleValue"] = tds;

  String payload;
  serializeJson(doc, payload);

  int httpResponseCode = http.POST(payload);

  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("HTTP Response code: " + String(httpResponseCode));
    Serial.println("Response: " + response);
  } else {
    Serial.print("Error en la solicitud HTTP. Código: ");
    Serial.println(httpResponseCode);
    Serial.print("Error: ");
    Serial.println(http.errorToString(httpResponseCode).c_str());
  }

  http.end();

  Serial.println("Datos enviados a Firestore:");
  Serial.println(payload);
}
