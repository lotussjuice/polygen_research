package cl.ubiobio.silkcorp.polygen_research.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Permisos publicos
                .requestMatchers("/login", "/register", "/css/**", "/js/**").permitAll()

                //  Permisos específicos por rol

                // DEV y ADMIN tienen acceso a todo 
                // Usamos hasAnyRole para permitir a cualquiera de los dos
                .requestMatchers("/dev/**").hasAnyRole("DEV", "ADMINISTRADOR") // En caso de añadir rutas de devs

                // ADMIN puede gestionar usuarios, roles y credenciales
                .requestMatchers("/usuarios/**", "/roles/**", "/whitelist/**").hasAnyRole("ADMINISTRADOR", "DEV")

                // INVESTIGADOR y ADMIN pueden gestionar datos clínicos
                .requestMatchers("/pacientes/**", "/crf/**", "/campos/**").hasAnyRole("INVESTIGADOR", "ADMINISTRADOR")

                // En casod de querer restringir a admin
                .requestMatchers("/registros/**").hasAnyRole("ADMINISTRADOR", "INVESTIGADOR")

                // La página de inicio es para cualquier usuario logueado
                .requestMatchers("/inicio", "/datos-crf/**").authenticated() // Cualquier rol logueado

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/inicio", true) // Redirige a /inicio al loguearse
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );            

            

        return http.build();
    }
}