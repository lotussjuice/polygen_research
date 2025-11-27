package cl.ubiobio.silkcorp.polygen_research.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    private final UserDetailsService userDetailsService; 

    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        
        // CLAVE: Esto permite que la DisabledException (Cuenta eliminada) 
        // llegue al AuthController en lugar de convertirse en "Bad Credentials"
        authProvider.setHideUserNotFoundExceptions(false); 
        
        return authProvider;
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // Permisos públicos (Login, Registro, Estilos)
                .requestMatchers("/login", "/register/**", "/forgot-password/**", "/reset-password/**", "/css/**", "/js/**").permitAll()

                // Permisos solo para DEV
                .requestMatchers("/usuarios/**", "/roles/**", "/whitelist/**").hasAnyRole("DEV","ADMINISTRADOR")

                // Permisos para DEV, ADMINISTRADOR, e INVESTIGADOR
                .requestMatchers(
                    "/pacientes/**", 
                    "/crf/**", 
                    "/campos/**", 
                    "/datos-crf/**",  // "Ver Datos (Valores)"
                    "/registros/**"   // "Historial de modificacion"
                ).hasAnyRole("DEV", "ADMINISTRADOR", "INVESTIGADOR")

                
                .requestMatchers("/inicio", "/dashboard/**").authenticated() 

                .anyRequest().authenticated() // Cualquier otra URL requiere al menos estar logueado
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/inicio", true) // Siempre redirige a /inicio
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