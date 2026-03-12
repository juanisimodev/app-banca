package com.banca.app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba basica de carga del contexto de Spring.
 */
@SpringBootTest
@ActiveProfiles("test")
class AppApplicationTests {

	@Test
	void contextLoads() {
	}

}
