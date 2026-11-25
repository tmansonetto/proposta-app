package com.tmansonetto.proposta_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuração de CORS (Cross-Origin Resource Sharing).
 *
 * <p><b>O problema:</b> por segurança, o navegador bloqueia chamadas JavaScript feitas de uma
 * "origem" (protocolo + domínio + porta) para outra. O front-end Angular roda em
 * {@code http://localhost:4200} e esta API em {@code http://localhost:8080}: são origens
 * diferentes, então, sem esta configuração, o navegador recusaria as respostas da API.
 *
 * <p><b>A solução:</b> a API informa ao navegador, por meio de cabeçalhos HTTP, quais origens
 * têm permissão para chamá-la. Ferramentas como o Postman não são navegadores e por isso
 * não sofrem essa restrição.
 *
 * <p>{@code @Configuration} indica ao Spring que esta classe contém configurações.
 * Ao implementar {@link WebMvcConfigurer}, podemos personalizar partes do Spring MVC
 * sobrescrevendo apenas os métodos que nos interessam.
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    /**
     * Define as regras de CORS da aplicação.
     *
     * <ul>
     *   <li>{@code addMapping("/**")}: aplica a regra a todos os endpoints da API;</li>
     *   <li>{@code allowedOrigins(...)}: lista as origens autorizadas a chamar a API;</li>
     *   <li>{@code allowedMethods("*")}: libera todos os métodos HTTP (GET, POST, PUT, DELETE...);</li>
     *   <li>{@code allowCredentials(true)}: permite o envio de cookies/credenciais nas requisições.
     *       Por isso as origens precisam ser listadas uma a uma (não é permitido usar {@code "*"}
     *       junto com credenciais).</li>
     * </ul>
     *
     * @param regisry objeto fornecido pelo Spring onde as regras de CORS são registradas
     */
    @Override
    public void addCorsMappings(CorsRegistry regisry){
        regisry.addMapping("/**").allowedOrigins("http://localhost", "http://localhost:4200").allowedMethods("*").allowCredentials(true);
    }
}
