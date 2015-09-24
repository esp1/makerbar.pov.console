/*
 * 8 Game controller buttons:
 *    X      up    O
 *   left        right
 * triangle down square
 *
 * Each controller can be in one of 9 states:
 * psxLeft  0x0001
 * psxDown  0x0002
 * psxRight 0x0004
 * psxUp    0x0008
 *
 * psxSqu   0x0100
 * psxX     0x0200
 * psxO     0x0400
 * psxTri   0x0800
 *
 * nothing  0x0000
 *
 * Serial protocol:
 *   1 byte = 0xAA - frame start
 *   2 bytes, mask=0x0F0F - controller A button value
 *   2 bytes, mask=0x0F0F - controller B button value
 *   1 byte = 0xFF - frame end
 */
#include <Psx.h>

#define PAD_A 0x01
#define PAD_B 0x02

Psx psxA;
Psx psxB;

void setup()
{
  // setupPins(dataPin, cmndPin, attPin, clockPin, delay_microseconds);
  psxA.setupPins(4, 5, 6, 7, 10);
  psxB.setupPins(8, 9, 10, 11, 10);
  Serial.begin(115200);
}

void writeButtonValue(unsigned int data) {
    Serial.write(data & 0x000F);
    Serial.write((data & 0x0F00) >> 8);

//    Serial.print("0");
//    Serial.print(data & 0x000F);
//    Serial.print("0");
//    Serial.print((data & 0x0F00) >> 8);
}

void loop()
{
  Serial.write(0xAA);  // frame start

//  Serial.print("A: ");
  writeButtonValue(psxA.read() & 0x0F0F);

//  Serial.print(", B: ");
  writeButtonValue(psxB.read() & 0x0F0F);

  Serial.write(0xFF);  // frame end
//  Serial.println();
  
  delay(20);
}

