#include <rgb_lcd.h>

#include <Servo.h> 
#include <Wire.h>

#include <SPI.h>
#include <Ethernet.h>

#define CLIENT_MSG_BUFFER 512 //Tamaño de buffer para almacenar mensaje del cliente
byte mac[] = { 0x98, 0x4f, 0xee, 0x01, 0x0c, 0x04 };  // MAC de la interfaz ethernet de la placa Galileo

IPAddress ip(192,168,10,141); //La IP fija que se desea configurar a la Galileo ( se puede omitir parmetro y usar DHCP )
  IPAddress gateway(192,168,10,1);
  IPAddress subnet(255,255,255,0);
EthernetServer server(8080); //El server se expondra en el puerto 80

Servo servo;
rgb_lcd lcd;

int pinButton = 4;
int pinServo = 3;
int pinPotenc = 0;
int pinLed = 13;
int valPotencNumber;
char strPotencNumber[10];

int intentosFallidos = 0;
int isOpen = 1;
char password[4];
char passwordInput[4];
char passwordAdmin[4];
char passwordAdminInput[4];
char patron[10];
char patronInput[10];
char palabra[20];
char palabraInput[20];
int passwordCurPos = 0;

boolean flag_client= false;
boolean flag_admin = false;

void setup() { 
  pinMode( pinButton, INPUT );
  pinMode( pinPotenc, INPUT );
  pinMode( pinLed, OUTPUT );
  Serial.begin(9600);   
  servo.attach( pinServo );
  servo.write(90);
  delay(500);
  servo.detach();

  lcd.begin(16, 2);
  lcd.clear();
  lcd.display();
  lcd.setRGB(0,255, 255);
  digitalWrite( pinLed, LOW );
  Serial.print("Hay alguien!");
  Ethernet.begin(mac, ip, gateway, subnet); //Se inicia conexion de red en placa
  server.begin(); //Se inicia webserver
  
} 
 
void loop() { 
  int msg_position = 0;
  //Bloque para manejo de respuesta de webserver a clientes
  if(flag_admin){
    EthernetClient client = server.available();
    if (client == true) {
      char msg_buffer[CLIENT_MSG_BUFFER];
      
      char c;
      Serial.print("Hay alguien!");
      Serial.print(client.connected());
      flag_client = true;
      while (client.connected() && client.available() ) {
          //Serial.println("ciclo while");
        if(strstr(msg_buffer, "User-Agent") != 0) break;
        if ( (c = client.read()) > 0 && c != '\r'){  //Mientras haya caracteres que leer, se almacenan
          //Serial.print(c);
          if (c == '\n') { //Nueva linea se reemplaza por espacio
            msg_buffer[msg_position++] = ' ';
            //Serial.println("Termina");
            
          } else if (c != '\r') { //Se almacena nuevo caracter valido
            msg_buffer[msg_position++] = c;
          }else{
            
            
              
          }
          
        } else{
               
        }
  
              
        
      }
  
      Serial.print(msg_buffer);
              
              //Se termino el mensaje, procesar y responder        
              if(msg_position > 0){
                if (strstr(msg_buffer, "GET /security/input_password?password=") != 0) { // Validacion de pin de admin (falta agregar codigo para pedirlo y almacenarlo)
                  Serial.print("Posicion: ");
                  passwordAdminInput[0] = msg_buffer[38];
                  passwordAdminInput[1] = msg_buffer[39];
                  passwordAdminInput[2] = msg_buffer[40];
                  passwordAdminInput[3] = msg_buffer[41];

                  //client.println("{ \"isValid\": \"true\" }");
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");

                  if(passwordAdminInput[0] == passwordAdmin[0] && passwordAdminInput[1] == passwordAdmin[1] && passwordAdminInput[2] == passwordAdmin[2] && passwordAdminInput[3] == passwordAdmin[3]){
                    client.println("{ \"isValid\": \"true\" }");
                  }else{
                    client.println("{ \"isValid\": \"false\" }");
                  }
                  passwordAdminInput[0] = ' ';
                  passwordAdminInput[1] = ' ';
                  passwordAdminInput[2] = ' ';
                  passwordAdminInput[3] = ' ';
                  
                } else if (strstr(msg_buffer, "GET /status_locker") != 0) { // Obtener estado
                
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  if(intentosFallidos == 3){
                    client.println("{ \"status\": \"2\" }");  
                  } else if(isOpen==1){
                    client.println("{ \"status\": \"0\" }");
                  } else {
                    client.println("{ \"status\": \"1\" }");
                  }
                  
                } else if (strstr(msg_buffer, "GET /status_patron") != 0) { // Obtener estado
                  
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  Serial.println("PATPAT");
                  if(patron[0] == 'I' || patron[0] == 'D' || patron[0] == 'A' || patron[0] == 'F'){
                    client.println("{ \"status\": \"1\" }"); 
                    Serial.println(" HAY PATRON"); 
                  } else {
                    client.println("{ \"status\": \"0\" }");
                    Serial.println("NO HAY PATRON");
                  } 
                  
                }else if (strstr(msg_buffer, "GET /set/patron?patron=") != 0) { // Obtener estado
                  
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                
                  int i1=23;
                  while(msg_buffer[i1] != ' ' && i1<33){
                    patron[i1-23] = msg_buffer[i1];
                    i1++;
                  }
                  Serial.println("Patron New: ");
                  Serial.println(patron);       
                  client.println("{ \"result\": \"1\" }");   
                  
                }else if (strstr(msg_buffer, "GET /set/palabra?palabra=") != 0) { // Obtener estado
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                
                  int i1=25;
                  while(msg_buffer[i1] != ' ' && i1<45){
                    palabra[i1-25] = tolower(msg_buffer[i1]);
                    i1++;
                  }
                  Serial.println("Palabra New: ");
                  Serial.println(palabra);       
                  client.println("{ \"result\": \"1\" }");            
                  
                } else if (strstr(msg_buffer, "GET /security/input_patron?patron=") != 0) { // Validacion de pin de admin (falta agregar codigo para pedirlo y almacenarlo)
                  
                  int i1=34;
                  while(msg_buffer[i1] != ' ' && i1<44){
                    patronInput[i1-34] = msg_buffer[i1];
                    i1++;
                  }
                  

                  //client.println("{ \"isValid\": \"true\" }");
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  Serial.println("Patron: ");
                  Serial.println(patron);
                  Serial.println("Patron New: ");
                  Serial.println(patronInput);
                  
                  if(strcmp(patron, patronInput) == 0){
                    client.println("{ \"isValid\": \"true\" }");
                  }else{
                    client.println("{ \"isValid\": \"false\" }");
                  }
                  for(int j = 0; j<10; j++)
                    patronInput[j] = '\0';
                 
                  
                } else if (intentosFallidos < 3 && strstr(msg_buffer, "GET /security/open") != 0) { // Modificar estado
                
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  client.println("{ \"result\": \"1\" }");  
                  servo.attach( pinServo );
                  servo.write(90);
                  delay(500);
                  servo.detach();

        
                  digitalWrite( pinLed, LOW );
                  isOpen = 1;
                  lcd.clear();
                  lcd.print( "Puerta" );
                  lcd.setCursor(0,1);
                  lcd.print( "Abierta" );
                  lcd.setRGB(0,255,0);
                  delay(3000);
                  //Hacer refactor para crear funcion abrir
                  //Hacer refactor para crear fucncion cerrar
                  
                }else if (intentosFallidos < 3 && strstr(msg_buffer, "GET /security/close") != 0) { // Modificar estado
                
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  client.println("{ \"result\": \"1\" }");  
  
                  servo.attach( pinServo );
                  servo.write(180);
                  delay(500);
                  servo.detach();

                  password[0] = passwordAdmin[0];
                  password[1] = passwordAdmin[1];
                  password[2] = passwordAdmin[2];
                  password[3] = passwordAdmin[3];
                  
                  isOpen = 0;
                  lcd.clear();
                  lcd.print( "Puerta" );
                  lcd.setCursor(0,1);
                  lcd.print( "Cerrada" );
                  lcd.setRGB(255,130,0);                
                  //Hacer refactor para crear funcion abrir
                  //Hacer refactor para crear fucncion cerrar
                  delay(3000);
                } else if(intentosFallidos == 3 &&  strstr(msg_buffer, "GET /security/unlock?palabra=") != 0) { // Modificar estado

                  int i1=29;
                  while(msg_buffer[i1] != ' ' && i1<49){
                    palabraInput[i1-29] = tolower(msg_buffer[i1]);
                    i1++;
                  }
                
                  client.println("HTTP/1.1 200 OK");
                  client.println("Content-Type: applicationo/json;charset=utf-8");
                  client.println("Content-Type: text/html");
                  client.println("Server: Arduino");
                  client.println("Connection: close");
                  client.println("");
                  Serial.println("");
                  Serial.println(palabra);
                  Serial.println(palabraInput);
                  if(strcmp(palabra, palabraInput) == 0){
                    client.println("{ \"result\": \"1\" }");  
                    

    
                    servo.attach( pinServo );
                    servo.write(90);
                    delay(500);
                    servo.detach();
          
                    digitalWrite( pinLed, LOW );
                    isOpen = 1;
                    intentosFallidos = 0;
                    lcd.print( "Puerta" );
                    lcd.setCursor(0,1);
                    lcd.print( "Desbloqueada" );
                    lcd.setRGB(0,255,0);   
                        
                    delay(3000);
                  }else{
                    client.println("{ \"result\": \"0\" }");   
                  }
                  for(int j = 0; j<20; j++)
                    palabraInput[j] = '\0';
                } else {
                  client.println("HTTP/1.1 404 Not Found");
                  client.println("Content-Type: application/json");          
                }
                
                
            
          
              }
              delay(50);
              for(int i = 0; i<512;i++){
                msg_buffer[i] = ' ';
              }
              client.stop(); //Cerrar conexion con cliente       
    }
    if(intentosFallidos == 3){
      lcd.clear();
      lcd.print( "Caja" );
      lcd.setCursor(0,1);
      lcd.print( "Bloqueada" );
      lcd.setRGB(255,0, 0);   
         
      
       passwordCurPos = 0;
    } else if( passwordCurPos < 4) {
  
      updateCurrentNumber();
  
      if( isButtonPressed() ) {
        sprintf( strPotencNumber, "%d", valPotencNumber );
        passwordInput[passwordCurPos] = strPotencNumber[0];
        passwordCurPos++;
      }
  
    } else {
        lcd.clear();
        if( isOpen == 1 ) {
          digitalWrite( pinLed, HIGH );
          for( int i = 0; i < 4; i++ ) {
            password[i] = passwordInput[i];
          }
  
          servo.attach( pinServo );
          servo.write(180);
          delay(500);
          servo.detach();
  
          isOpen = 0;
          lcd.print( "Puerta" );
          lcd.setCursor(0,1);
          lcd.print( "Cerrada" );
          lcd.setRGB(255,130,0);
  
        } else if ( isValidPasswordInput() ) {
  
            servo.attach( pinServo );
            servo.write(90);
            delay(500);
            servo.detach();
  
            digitalWrite( pinLed, LOW );
            isOpen = 1;
            lcd.print( "Puerta" );
            lcd.setCursor(0,1);
            lcd.print( "Abierta" );
            lcd.setRGB(0,255,0);
        } else {
            lcd.print( "Clave" );
            lcd.setCursor(0,1);
            lcd.print( "invalida" );
            lcd.setRGB(255,0, 0);
            intentosFallidos++;
        }
  
        delay(3000);
        lcd.clear();
        passwordCurPos = 0;
    }
  } else {
    if( passwordCurPos < 4) {
  
      updateCurrentNumberAdmin();
  
      if( isButtonPressed() ) {
        sprintf( strPotencNumber, "%d", valPotencNumber );
        passwordAdmin[passwordCurPos] = strPotencNumber[0];
        passwordCurPos++;
      }
    }else{
      passwordCurPos = 0;
      flag_admin = true;      
    }
  }
  delay(200);
}

void updateCurrentNumber() {
  valPotencNumber = map( analogRead(pinPotenc), 0, 1023, 0, 9);
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.setRGB(0,255,255);
  lcd.print("Ingrese clave");
  for( int i = 0; i < passwordCurPos; i++ ) {
    lcd.setCursor(i,1);
    lcd.print( passwordInput[i] );
  }
  lcd.setCursor(passwordCurPos,1);
  lcd.print( valPotencNumber );  
}

void updateCurrentNumberAdmin() {
  valPotencNumber = map( analogRead(pinPotenc), 0, 1023, 0, 9);
  lcd.clear();
  lcd.setCursor(0,0);
  lcd.setRGB(0,255,255);
  lcd.print("Clave Admin");
  for( int i = 0; i < passwordCurPos; i++ ) {
    lcd.setCursor(i,1);
    lcd.print( passwordAdmin[i] );
  }
  lcd.setCursor(passwordCurPos,1);
  lcd.print( valPotencNumber );  
}

int isButtonPressed() {
  return digitalRead( pinButton ) == HIGH;
}

int isValidPasswordInput() {
  int isValid = 1;
  for( int i = 0; i < 4; i++ ) {
     if( password[i] != passwordInput[i] ) {
       isValid = 0;
       break;
     }
  }
  return isValid;
}

boolean isNumeric(char c){
  if(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7' || c == '8' || c == '9'){
    return true;
  }else{
    return false;
  }
}

