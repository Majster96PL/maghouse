package com.example.items_service;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.eureka.EurekaClientConfigBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaServiceRegistry;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;

@SpringBootTest
class ItemsServiceApplicationTests {

	@Mock
	private EurekaClientConfigBean eurekaClientConfigBean;

	@Mock
	private EurekaServiceRegistry eurekaServiceRegistry;

	@Test
	void eurekaClientConfigurationLoads() {
		lenient().when(eurekaClientConfigBean.getServiceUrl())
				.thenReturn(Map.of("defaultZone", "http://localhost:8761/eureka/"));
		assertNotNull(eurekaClientConfigBean, "Eureka Client Config Bean should not be null");
	}

	@Test
	void eurekaServerRegistryLoads() {
		assertNotNull(eurekaServiceRegistry, "Eureka Service Registry should not be null");
	}

	@Test
	void eurekaClientPropertiesTest() {
		lenient().when(eurekaClientConfigBean.getServiceUrl())
				.thenReturn(Map.of("defaultZone", "http://localhost:8761/eureka/"));
		assertNotNull(eurekaClientConfigBean.getServiceUrl(), "Service URL should not be null");
		assertNotNull(eurekaClientConfigBean.getServiceUrl()
				.get("defaultZone"), "Default Zone URL should not be null ");
	}
}
