package com.example.maghouse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestPropertySource("classpath:application-test.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class MagHouseApplicationTests {

	@Value("${spring.datasource.url}")
	private String datasourceUrl;

	@Test
	void contextLoads() {
		assertThat(datasourceUrl).isNotNull();
		System.out.println("Datasource URL: " + datasourceUrl);
	}

	@AfterAll
    static void keepApplicationRunning() throws InterruptedException {
		Thread.sleep(Long.MAX_VALUE);
	}
}
