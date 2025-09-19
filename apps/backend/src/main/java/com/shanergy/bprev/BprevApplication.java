package com.shanergy.bprev;

import com.shanergy.bprev.config.StorageProperties;
import com.shanergy.bprev.integration.monitoring.MonitoringProperties;
import com.shanergy.bprev.integration.notification.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({StorageProperties.class, MonitoringProperties.class, NotificationProperties.class})
public class BprevApplication {

	public static void main(String[] args) {
		SpringApplication.run(BprevApplication.class, args);
	}

}
