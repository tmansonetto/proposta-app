package com.tmansonetto.proposta_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Classe principal da aplicação (ponto de entrada).
 *
 * <p>A anotação {@code @SpringBootApplication} reúne três outras anotações:
 * <ul>
 *   <li>{@code @Configuration}: indica que esta classe pode declarar configurações (beans);</li>
 *   <li>{@code @EnableAutoConfiguration}: pede ao Spring Boot que configure automaticamente
 *       o que encontrar no classpath (servidor web, JPA, banco de dados etc.);</li>
 *   <li>{@code @ComponentScan}: faz o Spring procurar, neste pacote e nos subpacotes,
 *       classes anotadas com {@code @RestController}, {@code @Service}, {@code @Repository}...
 *       e registrá-las como componentes gerenciados (beans).</li>
 * </ul>
 * Por isso esta classe fica no pacote "raiz": todos os outros pacotes estão abaixo dela.
 *
 * <p>{@code @EnableScheduling} liga o agendador de tarefas do Spring. Sem ela, os métodos
 * anotados com {@code @Scheduled} (como em {@code PropostaSemIntegracao}) nunca seriam executados.
 */
@SpringBootApplication
@EnableScheduling
public class PropostaAppApplication {

	/**
	 * Método {@code main}: é o primeiro método executado pela JVM.
	 * Ele delega ao {@link SpringApplication#run} a tarefa de subir o contexto do Spring,
	 * criar os beans, conectar ao banco e iniciar o servidor web embutido (Tomcat).
	 *
	 * @param args argumentos de linha de comando (opcionais) repassados ao Spring
	 */
	public static void main(String[] args) {
		SpringApplication.run(PropostaAppApplication.class, args);
	}

}
