package com.leightonobrien.pi.serialcomm;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Hello world!
 *
 */
public class App 
{
	private static SerialPort tacsPort = null;
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        commWithPort("USB-Serial");
        try{
        	Thread.sleep(10000);
        }catch(Exception e){
        	e.printStackTrace();
        }
        if (tacsPort == null){
        	System.out.println("No connection.");
        }else{
        	tacsPort.removeDataListener();
        	tacsPort.closePort();
        }
    }
    
    public static void commWithPort(String portname){
    	SerialPort[] commports = SerialPort.getCommPorts();
    	for (SerialPort port: commports){
		System.out.println(port.getPortDescription());
    		if (tacsPort==null && port.getDescriptivePortName().contains(portname)){
    			tacsPort = port;
    		}
    	}
    	if(tacsPort != null){
    		tacsPort.setComPortParameters(38400, 8, 1, SerialPort.NO_PARITY);
    		tacsPort.openPort();
    		tacsPort.addDataListener(new TacsListener()); 		
    	}
    }
}
