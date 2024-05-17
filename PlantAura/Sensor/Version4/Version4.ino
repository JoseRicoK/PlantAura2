#include <WiFi.h>               // Librería para manejo de WiFi
#include <WebServer.h>          // Librería para servidor web
#include <DNSServer.h>          // Librería para servidor DNS
#include <WiFiManager.h>        // Librería para manejo de WiFiManager, permite configurar WiFi sin hardcodear SSID y contraseña
#include <ESPmDNS.h>            // Librería para mDNS (Multicast DNS), permite descubrir servicios en la red local

// Crear una instancia del servidor web en el puerto 80
WebServer server(80);

// Declarar funciones
void handleRoot();
void handleNotFound();
void handleData();
void saveConfigCallback();

bool shouldSaveConfig = false;  // Variable para indicar si se debe guardar la configuración

void setup() {
  Serial.begin(115200);  // Inicializar la comunicación serial para depuración

  // Crear una instancia de WiFiManager
  WiFiManager wifiManager;

  // Callback para guardar la configuración
  wifiManager.setSaveConfigCallback(saveConfigCallback);

  // Iniciar el modo AP y servidor de configuración de WiFi
  if (!wifiManager.autoConnect("ESP32_AP_Config")) {
    Serial.println("No se pudo conectar y no se proporcionaron credenciales.");
    delay(3000);
    ESP.restart();
  }

  // Si se conecta a la red WiFi, inicia el servidor HTTP
  Serial.println("Conectado a la red WiFi.");
  Serial.print("Dirección IP: ");
  Serial.println(WiFi.localIP());

  // Iniciar el servicio mDNS
  if (!MDNS.begin("esp32")) {  // "esp32" es el nombre del dispositivo en la red local
    Serial.println("Error al iniciar mDNS");
    return;
  }
  Serial.println("mDNS iniciado");

  // Añadir servicio mDNS
  MDNS.addService("http", "tcp", 80);  // Anunciar un servicio HTTP en el puerto 80

  // Configurar rutas del servidor
  server.on("/", handleRoot);        // Ruta para la página raíz
  server.on("/data", handleData);    // Ruta para obtener datos del sensor
  server.onNotFound(handleNotFound); // Ruta para manejar páginas no encontradas

  // Iniciar el servidor
  server.begin();
  Serial.println("Servidor HTTP iniciado");
}

void loop() {
  server.handleClient();  // Manejar las peticiones HTTP entrantes

  // Si las credenciales fueron guardadas, puedes ejecutar alguna lógica aquí
  if (shouldSaveConfig) {
    Serial.println("Las credenciales WiFi fueron guardadas.");
    shouldSaveConfig = false;
  }
}

// Manejar la petición a la ruta raíz
void handleRoot() {
  Serial.println("Petición recibida en la ruta raíz");
  server.send(200, "text/plain", "ESP32 conectado correctamente.");  // Responder con un mensaje de texto
}

// Manejar la petición a la ruta /data
void handleData() {
  Serial.println("Petición recibida en la ruta /data");
  String message = "1234"; // Mensaje de prueba, puedes cambiarlo para leer un valor de un sensor
  server.send(200, "text/plain", message);  // Responder con el mensaje del sensor
}

// Manejar peticiones a rutas no encontradas
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

  server.send(404, "text/plain", message);  // Responder con detalles de la petición no encontrada
}

// Callback para guardar la configuración
void saveConfigCallback() {
  Serial.println("Se guardó la configuración.");
  shouldSaveConfig = true;  // Indicar que se debe guardar la configuración
}
