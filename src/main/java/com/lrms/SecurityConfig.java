package com.lrms;

import com.lrms.entity.Staff;
import com.lrms.repository.StaffRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import java.io.IOException;
import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/v3/api-docs/**", "/v3/api-docs", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/css/**")
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/css/**").permitAll()
                .requestMatchers("/", "/login", "/dashboard", "/rooms", "/bookings", "/guests", "/restaurant", "/billing", "/housekeeping", "/staff", "/test", "/usage-dashboard").permitAll()
                
                // RBAC for API
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/admin/staff").hasAnyRole("ADMIN", "MANAGER", "HOUSEKEEPING")
                .requestMatchers("/api/admin/staff/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/stats").authenticated()
                .requestMatchers("/api/admin/housekeeping/**").hasAnyRole("ADMIN", "MANAGER", "HOUSEKEEPING")
                
                // Cross-role read access (GET only) — placed before broad rules
                // Housekeeping page needs rooms & tables for task-assignment dropdowns
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/rooms/**").hasAnyRole("ADMIN", "MANAGER", "FRONT_DESK", "HOUSEKEEPING")
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/tables/**").hasAnyRole("ADMIN", "MANAGER", "RESTAURANT_STAFF", "HOUSEKEEPING")
                // Restaurant & billing pages need bookings for room-service lookup
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/bookings/**").hasAnyRole("ADMIN", "MANAGER", "FRONT_DESK", "RESTAURANT_STAFF")
                
                // Full access (all HTTP methods)
                .requestMatchers("/api/rooms/**", "/api/bookings/**", "/api/guests/**").hasAnyRole("ADMIN", "MANAGER", "FRONT_DESK")
                .requestMatchers("/api/menu/**", "/api/tables/**", "/api/orders/**").hasAnyRole("ADMIN", "MANAGER", "RESTAURANT_STAFF")
                .requestMatchers("/api/bills/**").hasAnyRole("ADMIN", "MANAGER", "FRONT_DESK", "RESTAURANT_STAFF")
                
                // External Integration API
                .requestMatchers("/api/external/**").hasAnyRole("ADMIN", "MANAGER", "PARTNER")
                
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Service
    public static class CustomUserDetailsService implements UserDetailsService {
        private final StaffRepository staffRepository;

        public CustomUserDetailsService(StaffRepository staffRepository) {
            this.staffRepository = staffRepository;
        }

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
            Staff staff = staffRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Staff not found with email: " + email));
            
            if (!staff.getIsActive()) {
                throw new UsernameNotFoundException("Staff account is inactive");
            }

            return org.springframework.security.core.userdetails.User
                    .withUsername(staff.getEmail())
                    .password(staff.getPasswordHash())
                    .roles(staff.getRole())
                    .build();
        }
    }

    @Component
    public static class JwtTokenProvider {
        @Value("${jwt.secret}")
        private String secret;

        @Value("${jwt.expiration}")
        private long expiration;

        public String generateToken(String username) {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        }

        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }

        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        private Claims extractAllClaims(String token) {
            Key key = Keys.hmacShaKeyFor(secret.getBytes());
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        }

        public Boolean validateToken(String token, String username) {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        }

        private Boolean isTokenExpired(String token) {
            return extractClaim(token, Claims::getExpiration).before(new Date());
        }
    }

    @Component
    public static class JwtAuthFilter extends OncePerRequestFilter {
        private final JwtTokenProvider jwtTokenProvider;
        private final UserDetailsService userDetailsService;

        public JwtAuthFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
            this.jwtTokenProvider = jwtTokenProvider;
            this.userDetailsService = userDetailsService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
                try {
                    username = jwtTokenProvider.extractUsername(token);
                } catch (Exception e) {
                    // Token invalid or expired
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtTokenProvider.validateToken(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);
        }
    }
}
