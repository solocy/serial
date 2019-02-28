package cn.videon.card.service;

import com.alibaba.fastjson.JSONObject;
import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Instant;

@Service
public class JSerialComm {


    private static final Logger log = LoggerFactory.getLogger(JSerialComm.class);

    private static SerialPort serialPort;

    @Autowired
    @Qualifier("myRedisTemplate")
    private StringRedisTemplate redisTemplate;

    public void start(String serial,String ipcIp) {
        log.debug("serial connecting...");
        // getting serial ports list into the array
        SerialPort[] commports = SerialPort.getCommPorts();
        for (SerialPort port: commports){
            log.debug(port.getPortDescription());
            if (serialPort==null && port.getDescriptivePortName().contains(serial)){
                log.debug("connecting serial is {}",port.getDescriptivePortName());
                serialPort = port;
            }
        }
        if(serialPort != null){
            serialPort.setComPortParameters(38400, 8, 1, SerialPort.NO_PARITY);
            serialPort.openPort();
            serialPort.addDataListener(new PortReader(ipcIp));
            log.debug("serial connect success!");
        }
        if (serialPort == null){
            log.debug("No connection.");
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("destroy serial connecting...");
        serialPort.removeDataListener();
        serialPort.closePort();
        log.info("destroy serial success!");
    }


    private class PortReader implements SerialPortDataListener {

        private String ipcIp;

        public PortReader(String ipcIp) {
            this.ipcIp =ipcIp;
        }

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                return;
            }
            try{
                Thread.sleep(100);
            }catch(Exception e){
                e.printStackTrace();
            }
            byte[] newData = new byte[event.getSerialPort().bytesAvailable()];
            int numRead=event.getSerialPort().readBytes(newData, newData.length);
            String receivedData = encodeHexString(newData);
            //redirect input to Tacs server
            log.debug("Received data of size: {}" , numRead);
            log.debug("Received data hex string: {}" , receivedData);

            if (receivedData.length()==42) {
                String status = receivedData.substring(receivedData.length()-6,receivedData.length()-4);
                receivedData = receivedData.substring(18,receivedData.length()-8);
                log.debug("截断信息：{}" , receivedData);
                if (receivedData.length() == 16) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("cardIpcIp",ipcIp);
                    jsonObject.put("cardRecordTime", Instant.now());
                    if ("01".equals(status)) {
                        jsonObject.put("status","IN");
                    } else if ("02".equals(status)) {
                        jsonObject.put("status","OUT");
                    }
                    if (!receivedData.endsWith("00")) {
                        log.debug("卡片id: {}" ,receivedData);
                        StringBuilder stringBuffer = new StringBuilder(receivedData);
                        StringBuilder result = new StringBuilder();
                        int i = 0 ;
                        while (stringBuffer.length()> i*2) {
                            result.append(stringBuffer.substring(stringBuffer.length()-2*i -2,stringBuffer.length()-2*i ));
                            i++;
                        }

                        log.debug("卡片di: {}" , result);
                        jsonObject.put("isCard",true);
                        jsonObject.put("cardId",result.toString());
                    } else {

                        jsonObject.put("isCard",false);
                        log.debug("无卡");
                    }
                    log.debug("redis is message : {}",jsonObject.toJSONString());
                    redisTemplate.convertAndSend("card_record",jsonObject.toJSONString());
                }

            }
        }
        public String encodeHexString(byte[] byteArray) {
            StringBuffer hexStringBuffer = new StringBuffer();
            for (int i = 0; i < byteArray.length; i++) {
                hexStringBuffer.append(byteToHex(byteArray[i]));
            }
            return hexStringBuffer.toString();
        }
        public String byteToHex(byte num) {
            char[] hexDigits = new char[2];
            hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
            hexDigits[1] = Character.forDigit((num & 0xF), 16);
            return new String(hexDigits);
        }

    }
}