package science.apolline.ioio;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;

public class IOIOService extends ioio.lib.util.android.IOIOService{

    boolean led = true;

    @Override
    protected IOIOLooper createIOIOLooper() {
        return new BaseIOIOLooper(){

            private DigitalOutput led_;
            private Uart uart_ = null;
            private InputStream uartIn_ = null;
            private AnalogInput inputTemp;
            private AnalogInput inputHum;

            private IOIOData data = new IOIOData();

            @Override
            protected void setup() throws ConnectionLostException,InterruptedException {
                led_ = ioio_.openDigitalOutput(0, true);
                uart_ = ioio_.openUart(7, 6, 9600, Uart.Parity.NONE, Uart.StopBits.ONE);
                uartIn_ = uart_.getInputStream();
                inputTemp = ioio_.openAnalogInput(44);
                inputHum = ioio_.openAnalogInput(42);
            }

            @Override
            public void loop() throws ConnectionLostException,InterruptedException {
                try {
                    int availableCount = this.uartIn_.available();
                    if (availableCount > 0) {
                        byte[] buffer = new byte[availableCount];
                        int col;
                        this.uartIn_.read(buffer);
                        for (int b : buffer) {
                            switch (b) {
                                case 0x42:
                                    data.setCount(0);
                                    break;
                                default:
                                    data.setBuff(data.getCount(), b & 0xff);
                                    data.setCount(data.getCount()+1);
                                    if (data.getCount() == data.LENG) {
                                        data.setCount(0);
                                        if (data.checkValue()) {
                                            led = !led;
                                            led_.write(led);

                                            //parse PM value
                                            data.parse();

                                            //Température
                                            float tensionTemp = inputTemp.getVoltage();
                                            data.setTempKelvin(tensionTemp*100);

                                            //humidity
                                            float tensionHum = inputHum.getVoltage();
                                            float RH = (float) (((tensionHum / 3.3) - 0.1515) / 0.0636); //pour avoir l'humidité à 25°C
                                            double RHT = (RH / (1.0546 - 0.00216 * ((tensionTemp * 100) - 273.15))) * 10; //compensé en t°
                                            data.setRH(RH);
                                            data.setRHT(RHT);


                                        }
                                    }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}