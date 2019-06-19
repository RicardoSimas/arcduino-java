package control;

import serialPort.SerialRxTx;

public class Controller {

    public static void main(String[] args) {
        SerialRxTx serial = new SerialRxTx();
        
        if(serial.iniciaSerial()){
            while (true) {                
                
            }
        }
        else{
            
        }
    }
    
}
