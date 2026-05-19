/*
 * Copyright (c) 2026 CrudCraft contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.datasteel.crudcraft.sample.security;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriterFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;


/** Security configuration for the sample application. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationConverter jwtAuthConverter) throws Exception {

        http.csrf(
                        csrf ->
                                csrf.csrfTokenRepository(
                                                CookieCsrfTokenRepository.withHttpOnlyFalse())
                                        .ignoringRequestMatchers(
                                                SecurityConfig::isStatelessApiRequest))
                .headers(
                        h ->
                                h.withObjectPostProcessor(new EagerHeaderWriterFilterProcessor())
                                        .frameOptions(
                                                HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                                auth ->
                                        auth
                                        // static assets
                                        .requestMatchers(
                                                PathRequest.toStaticResources().atCommonLocations())
                                        .permitAll()

                                        // public endpoints
                                        .requestMatchers(
                                                PathPatternRequestMatcher.pathPattern(
                                                        HttpMethod.POST, "/auth/login"))
                                        .permitAll()
                                        .requestMatchers(
                                                PathPatternRequestMatcher.pathPattern(
                                                        "/v3/api-docs/**"))
                                        .permitAll()
                                        .requestMatchers(
                                                PathPatternRequestMatcher.pathPattern(
                                                        "/swagger-ui/**"))
                                        .permitAll()
                                        .requestMatchers(
                                                PathPatternRequestMatcher.pathPattern(
                                                        "/swagger-ui.html"))
                                        .permitAll()

                                        // everything else
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.jwt(
                                        jwt ->
                                                jwt.jwtAuthenticationConverter(
                                                        jwtAuthConverter)));

        return http.build();
    }

    private static boolean isStatelessApiRequest(HttpServletRequest request) {
        if (hasBearerToken(request)) {
            return true;
        }
        String path = request.getRequestURI();
        return !path.startsWith("/h2-console")
                && !path.startsWith("/swagger-ui")
                && !path.startsWith("/v3/api-docs");
    }

    private static boolean hasBearerToken(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization != null && authorization.startsWith("Bearer ");
    }

    @Bean
    JwtDecoder jwtDecoder(@Value("${crudcraft.security.jwt.secret}") String secret) {
        // Sample-only symmetric JWT setup. Production deployments should use a JWK set.
        org.springframework.util.Assert.isTrue(
                secret.length() >= 32, "JWT secret must be at least 32 characters");
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        var roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("roles");
        roles.setAuthorityPrefix("ROLE_");
        var conv = new JwtAuthenticationConverter();
        conv.setJwtGrantedAuthoritiesConverter(roles);
        return conv;
    }

    private static final class EagerHeaderWriterFilterProcessor
            implements ObjectPostProcessor<HeaderWriterFilter> {

        @Override
        public <O extends HeaderWriterFilter> O postProcess(O filter) {
            filter.setShouldWriteHeadersEagerly(true);
            return filter;
        }
    }
}
