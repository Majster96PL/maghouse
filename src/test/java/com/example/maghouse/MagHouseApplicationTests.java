package com.example.maghouse;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@Suite
@IncludeEngines("junit-jupiter")
@SpringBootTest
@SelectPackages({
		"com.examples.maghouse.unit",
		"com.examples.maghouse.integration"
})
class MagHouseApplicationTests {

	@Test
	void contextLoads() {
	}

}
