import jssc.*;


public class JSerialComm {

    static SerialPort serialPort = new SerialPort("/dev/tty.usbserial");

    public static void main(String[] args) {
            try {
                serialPort.openPort();
                serialPort.setParams(SerialPort.BAUDRATE_38400,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);


                serialPort.addEventListener(new PortReader());
            }
            catch (SerialPortException ex) {
                System.out.println("There are an error on writing string to port т: " + ex);
            }
    }


    private static class PortReader implements SerialPortEventListener {


        @Override
        public void serialEvent(SerialPortEvent event) {
            if(event.getEventValue() > 1) {
                try {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException ex) {
                       ex.printStackTrace();
                    }
                    String receivedData = serialPort.readHexString();
                    receivedData = receivedData.replace(" ","");
                    System.out.println("愿数据：" + receivedData);
                    if (receivedData.length()>22) {
                        receivedData = receivedData.substring(18,receivedData.length()-8);
                        System.out.println("截断信息：" + receivedData);
                        if (receivedData.length() == 16) {
                            if (receivedData.endsWith("E0")) {
                                System.out.println("卡片id:" + receivedData);
                                StringBuffer stringBuffer = new StringBuffer(receivedData);
                                StringBuffer result = new StringBuffer();
                                int i = 0 ;
                                while (stringBuffer.length()> i*2) {
                                    result.append(stringBuffer.substring(stringBuffer.length()-2*i -2,stringBuffer.length()-2*i ));
                                    i++;
                                }
                                System.out.println("卡片di:" + result);
                            } else {
                                System.out.println("无卡");
                            }
                        }

                    }
                }
                catch (SerialPortException ex) {
                    System.out.println("Error in receiving string from COM-port: " + ex);
                }
            }
        }

    }

}