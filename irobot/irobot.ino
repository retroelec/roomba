#include <ESP8266WiFi.h>
#include <string.h>

#define SERCOM
#define PORT 2002

#define WIFI_SSID "ssid"
#define WIFI_PASS "pass"

char *ssid = WIFI_SSID;
const char *pass = WIFI_PASS;

IPAddress staticIP(192, 168, 1, 199);
IPAddress gateway(192, 168, 1, 1);
IPAddress subnet(255, 255, 255, 0);

WiFiServer server(PORT);

static char buffer[16];
static uint8_t bytbuf[200];

uint8_t sndjbellsp1[] = {76, 16, 76, 16, 76, 32, 76, 16, 76, 16, 76,
                         32, 76, 16, 79, 16, 72, 16, 74, 16, 76, 32,
                         77, 16, 77, 16, 77, 16, 77, 32, 77, 16};
uint8_t sndjbellsp2[] = {76, 16, 76, 32, 79, 16, 79,
                         16, 77, 16, 74, 16, 72, 32};

uint8_t packetsize0_58[] = {26, 10, 6, 10, 14, 12, 62, 1, 1, 1, 1, 1, 1, 1, 1,
                            1,  1,  1, 1,  2,  2,  1,  2, 2, 1, 2, 2, 2, 2, 2,
                            2,  2,  1, 2,  1,  1,  1,  1, 1, 2, 2, 2, 2, 2, 2,
                            1,  2,  2, 2,  2,  2,  2,  1, 1, 2, 2, 2, 2, 1};
uint8_t packetsize100_107[] = {80, 28, 0, 0, 0, 0, 12, 9};

#ifdef SERCOM
void roombawakeup() {
  digitalWrite(12, HIGH);
  delay(200);
  digitalWrite(12, LOW);
  delay(500);
  digitalWrite(12, HIGH);
}
#endif

#ifdef SERCOM
static void cleanuartbuffer() {
  // clean uart buffer
  while (Serial.available()) {
    Serial.read();
    delay(1);
  }
}
#endif

void setup() {
  Serial.begin(115200);

  // init. WiFi
  Serial.print("connecting to ");
  Serial.println(ssid);
  WiFi.config(staticIP, gateway, subnet);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());

  // init TCP server
  server.begin();

// init. BRC output
#ifdef SERCOM
  pinMode(12, OUTPUT);
#endif

// init. UART (default: 8 data bits, no parity, one stop bit)
#ifdef SERCOM
  delay(200);
  Serial.swap();
  delay(200);
#endif
}

void loop() {
  yield();

  // get client
  WiFiClient client = server.available();
  if (client) {
    if (client.connected()) {
#ifndef SERCOM
      Serial.println("client connected");
#endif
    }

    // loop while client is connected
    while (client.connected()) {

#ifdef SERCOM
      // clean uart buffer
      cleanuartbuffer();
#endif

      // receive command from client
      int i = 0;
      while (client.available() > 0) {
        buffer[i++] = client.read();
      }
      buffer[i] = '\0';
#ifndef SERCOM
      if (strlen(buffer) != 0) {
        Serial.println("received command:");
        Serial.println(buffer);
      }
#endif

      if (strncmp(buffer, "start", 5) == 0) {
        delay(100);
#ifdef SERCOM
        roombawakeup();
        delay(200);
        Serial.write(128); // start
#else
        Serial.println("cmd start, send ok");
#endif
        client.write("ok");
#ifdef SERCOM
        // clean uart buffer
        cleanuartbuffer();
#endif
      } else if (strncmp(buffer, "stop", 4) == 0) {
#ifdef SERCOM
        Serial.write(128); // passive mode
        // clean uart buffer
        cleanuartbuffer();
        delay(200); // wait 200ms
        // seek dock if not already in docking station
        Serial.write(142); // sensors
        Serial.write(34);  // charging sources available
        delay(100);
        uint8_t res = Serial.read();
        if ((res & 2) != 2) {
          Serial.write(143); // seek dock
        }
#else
        Serial.println("cmd stop, disconnect client");
#endif
        break;
      } else if (strncmp(buffer, "cmd1", 4) == 0) {
        uint8_t cmd = buffer[4] & 0xFF;
#ifdef SERCOM
        Serial.write(cmd);
#else
        Serial.println("cmd cmd1:");
        Serial.println(cmd);
#endif
        client.write("ok");
      } else if (strncmp(buffer, "cmd2", 4) == 0) {
        uint8_t cmd = buffer[4] & 0xFF;
#ifdef SERCOM
        Serial.write(cmd);
        uint8_t numofbytes = buffer[5] & 0xFF;
        for (uint8_t i = 0; i < numofbytes; i++) {
          Serial.write(buffer[6 + i] & 0xFF);
        }
#else
        Serial.println("cmd cmd2:");
        Serial.println(cmd);
#endif
        client.write("ok");
      } else if (strncmp(buffer, "sens", 4) == 0) {
        uint8_t numofrecvbytes = 0;
        uint8_t numofpackets = buffer[4] & 0xFF;
#ifdef SERCOM
        Serial.write(149); // query list
        Serial.write(numofpackets);
        for (uint8_t i = 0; i < numofpackets; i++) {
          uint8_t packet = buffer[5 + i] & 0xFF;
          if (packet < 100) {
            numofrecvbytes += packetsize0_58[packet];
          } else {
            numofrecvbytes += packetsize100_107[packet - 100];
          }
          Serial.write(packet);
        }
        delay(100);
        for (i = 0; i < numofrecvbytes; i++) {
          bytbuf[i] = Serial.read();
          delay(1);
        }
#else
        Serial.println("cmd sens, num of packets:");
        Serial.println(numofpackets);
#endif
        client.write(bytbuf, numofrecvbytes);
      } else if (strncmp(buffer, "drive", 5) == 0) {
#ifdef SERCOM
        uint8_t vl = buffer[5] & 0xFF;
        uint8_t vh = buffer[6] & 0xFF;
        uint8_t rl = buffer[7] & 0xFF;
        uint8_t rh = buffer[8] & 0xFF;
        Serial.write(137); // drive
        Serial.write(vh);
        Serial.write(vl);
        Serial.write(rh);
        Serial.write(rl);
#endif
      } else if (strncmp(buffer, "xmas", 4) == 0) {
#ifdef SERCOM
        Serial.write(131); // safe mode
        delay(200);
        Serial.write(137); // drive
        Serial.write(0);
        Serial.write(100);
        Serial.write(0);
        Serial.write(1);
        Serial.write(164); // digit LEDs ASCII
        Serial.write(76);  // L
        Serial.write(73);  // I
        Serial.write(69);  // E
        Serial.write(66);  // B
        Serial.write(140); // program song
        Serial.write(0);   // song number
        Serial.write(16);  // song length
        Serial.write(sndjbellsp1, 32);
        Serial.write(140); // program song
        Serial.write(1);   // song number
        Serial.write(7);   // song length
        Serial.write(sndjbellsp2, 14);
        Serial.write(141); // play song
        Serial.write(0);   // song number
        // wait for song 0 to end
        int songplaying = 1;
        int i = 0;
        while (songplaying) {
          Serial.write(139);           // LEDs
          Serial.write(i & 15);        // LED bits
          Serial.write((i * 4) & 255); // color of power LED
          Serial.write(255);           // intensity of power LED
          Serial.write(142);           // sensors
          Serial.write(37);            // song playing
          delay(200);                  // wait 200ms
          songplaying = Serial.read();
          i++;
          if (i == 80)
            break;
        }
        Serial.write(141); // play song
        Serial.write(1);   // song number
        delay(4000);
        Serial.write(137); // drive
        Serial.write(0);
        Serial.write(0);
        Serial.write(128);
        Serial.write(0);
#endif
      }
    }
    client.stop();
#ifndef SERCOM
    Serial.println("client disconnected");
#endif
  }
}
