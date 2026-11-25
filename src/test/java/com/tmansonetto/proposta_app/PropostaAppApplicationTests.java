package com.tmansonetto.proposta_app;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de "fumaça" (smoke test) da aplicação.
 *
 * <p>{@code @SpringBootTest} sobe a aplicação inteira, como se ela estivesse rodando de verdade.
 * Por isso este teste precisa do PostgreSQL disponível (e da variável {@code DB_PASSWORD}).
 */
@SpringBootTest
class PropostaAppApplicationTests {

	/**
	 * Verifica se o contexto do Spring carrega sem erros. O método é vazio de propósito:
	 * se algum bean estiver mal configurado, o teste falha antes mesmo de chegar aqui.
	 */
	@Test
	void contextLoads() {
	}

}
