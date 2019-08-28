package cn.videon.card;

import cn.videon.card.config.ApplicationProperties;
import cn.videon.card.service.JSerialComm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties({ApplicationProperties.class})
public class CardApplication {

    @Autowired
    private JSerialComm jSerialComm;

    @Autowired
    private ApplicationProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(CardApplication.class, args);
    }

    @Bean
    public void start() {
        jSerialComm.start(properties.getSerial());
    }

}
