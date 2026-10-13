package com.ecommerce.ecommerce_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"api.security.jwt.secret=chave_de_teste_com_mais_de_32_caracteres_ok",
		"api.security.jwt.expiration-millis=3600000"
})
class EcommerceApiApplicationTests {

	@Test
	void contextLoads() {
	}
}