package kg.almalab.meddocs.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. CORS должен быть ПЕРВЫМ
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 2. Отключаем CSRF
                .csrf(AbstractHttpConfigurer::disable)

                // 3. Настройка разрешений
                .authorizeHttpRequests(auth -> auth
                        // Разрешаем OPTIONS для всех эндпоинтов (Критично для CORS)
                        .requestMatchers(org.springframework.web.cors.CorsUtils::isPreFlightRequest).permitAll()
                        // Разрешаем всё, что связано с авторизацией
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // Разрешаем корень (чтобы не было 403, когда заходишь по ссылке)
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/error").permitAll()
                        // Остальное — под замок
                        .anyRequest().authenticated()
                )

                // 4. Stateless сессии
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 5. Твой фильтр
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//
//        http.csrf().disable() // отключаем CSRF для разработки
//                .cors()            // включаем CORS поддержку
//                .and()
//                .authorizeRequests()
//                .antMatchers("/auth/**", "/templates/inbox-admin", "/templates/inbox-super").permitAll()
//                .anyRequest().authenticated()
//                .and()
//                .httpBasic();
//
//        return http.build();
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Разрешаем все твои домены
        config.setAllowedOrigins(Arrays.asList(
                "https://almalabfrontprod.vercel.app","https://tissue-story-illustrations-defined.trycloudflare.com"
        ));

        // Стандартные методы
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));

        // Обязательно разрешаем заголовок Content-Type и Authorization
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With", "Origin", "ngrok-skip-browser-warning", "localto-skip-warning"));
        // Разрешаем куки и заголовки авторизации
        config.setAllowCredentials(true);

        // Кэшируем CORS ответ на час
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Исправлено: просто передаем config, без лямбды здесь (в стандартном методе)
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
