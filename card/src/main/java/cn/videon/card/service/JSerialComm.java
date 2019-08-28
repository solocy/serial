package cn.videon.card.service;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.time.Instant;

@Service
public class JSerialComm {


    private static final Logger log = LoggerFactory.getLogger(JSerialComm.class);

    private static SerialPort serialPort;


    private static Instant heartbeatTime = Instant.now();

    private String serial;

    // 开启尝试连接设备
    public void start(String serial) {
        log.debug("serial connecting...");
        this.serial = serial;
        // 循环获取串口转usb设备
        while (true) {
            log.debug("select serial port");
            SerialPort[] commports = SerialPort.getCommPorts();
            for (SerialPort port : commports) {
                log.debug(port.getDescriptivePortName());
                if (serialPort == null && port.getDescriptivePortName().contains(serial)) {
                    log.debug("connecting serial is {}", port.getDescriptivePortName());
                    serialPort = port;
                }
            }
            if (serialPort != null) {
                serialPort.setComPortParameters(38400, 8, 1, SerialPort.NO_PARITY);
                if (!serialPort.openPort()) {
                    start(serial);
                    return;
                }
                serialPort.addDataListener(new PortReader());
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
        log.debug("serial connect success!");
        cardHeartBeat();
    }

    // 心跳监控， 心跳断开后马上重新尝试连接设备
    public void cardHeartBeat() {
        log.debug("开启读卡心跳测试");
        new Thread(() -> {
            while (true) {
                log.debug("心跳测试");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
                Instant now = Instant.now();
                if (now.toEpochMilli() - heartbeatTime.toEpochMilli() > 10000) {
                    log.info(" card connect failure");
                    log.info("destroy serial connecting...");
                    serialPort.removeDataListener();
                    serialPort.closePort();
                    log.info("destroy serial success!");
                    start(serial);
                    return;
                } else if (now.toEpochMilli() - heartbeatTime.toEpochMilli() < 10000) {
                    log.info(" card connect success");
                }
            }
        }).start();
    }

    @PreDestroy
    public void destroy() {
        log.info("destroy serial connecting...");
        serialPort.removeDataListener();
        serialPort.closePort();
        log.info("destroy serial success!");
    }


    private class PortReader implements SerialPortDataListener {

        @Override
        public int getListeningEvents() {
            return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
        }

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                return;
            }
            // todo 处理接收到的消息
        }


        // byte 转 16进制
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