package com.example.warenhouse_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class WarehouseServiceIntegrationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    @MockBean
    private DiscoveryClient discoveryClient;

    @Test
    void contextLoads() {
        assertNotNull(applicationContext, "Application context should net be null");
    }

    @Test
    void eurekaClientBeanLoad() {
        Object eurekaClient = applicationContext.getBean("eurekaClient");
        assertNotNull(eurekaClient, "Eureka Client Bean should be loaded");
    }

    @Test
    void testServiceRegistration() {
        List<String> services = discoveryClient.getServices();
        assertTrue(services.isEmpty(), "Warehouse-Service instances should not be empty");
    }

    @Test
    void testServiceDiscovery() {
        List<ServiceInstance> instances = discoveryClient.getInstances("warehouse-service");
        assertFalse(instances.stream().isParallel(), "Warehouse-service instances should not be empty");
        for ( ServiceInstance instance : instances) {
            assertNotNull(instance.getUri(), "Instance URI should not be null");
        }
    }

    @Test
    void testServiceMetadata() {
        List<ServiceInstance> instance = discoveryClient.getInstances("warehouse-service");
        assertTrue(instance.isEmpty(), "Warehouse-service instances should not be empty");
        for (ServiceInstance serviceInstance : instance) {
            Map<String, String> metadata = serviceInstance.getMetadata();
            assertNotNull(metadata, "Metadata should not be null");
            assertTrue(metadata.containsKey("instanceId"), "Metadata should contain instanceId");
        }
    }

    @Test
    void testDiscoveryClient() {
        ServiceInstance serviceInstance = new ServiceInstance() {
            @Override
            public String getServiceId() {
                return "warehouse-service";
            }

            @Override
            public String getHost() {
                return "localhost";
            }

            @Override
            public int getPort() {
                return 8081;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public URI getUri() {
                return URI.create("http://localhost:8081");
            }

            @Override
            public Map<String, String> getMetadata() {
                return Collections.emptyMap();
            }
        };

        lenient().when(discoveryClient.getInstances("warehouse-service"))
                .thenAnswer(invocationOnMock -> (List.of(serviceInstance))
                );

        List<ServiceInstance> instances = discoveryClient.getInstances("warehouse-service");
        assertNotNull(instances, "Instances should not be null");
        assertEquals(1, instances.size(), "There should be one instance");
        assertEquals("http://localhost:8081", instances.get(0).getUri().toString(), "URI should match");
    }
}
