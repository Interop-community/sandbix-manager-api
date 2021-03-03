//package org.hspconsortium.sandboxmanagerapi.security;
//
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.Ordered;
//import org.springframework.core.annotation.Order;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.web.access.channel.ChannelProcessingFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//@Configuration
//@EnableWebSecurity
//public class ServerSecurityConfig extends WebSecurityConfigurerAdapter {
//
//    @Value("${hspc.platform.authorization.tokenEndpoint}")
//    private String checkTokenEndpointUrl;
//
//    @Value("${hspc.platform.authorization.adminAccess.clientId}")
//    private String adminClientId;
//
//    @Value("${hspc.platform.authorization.adminAccess.clientSecret}")
//    private String adminClientSecret;
//
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    public InvalidMediaTypeFilter invalidMediaTypeFilter() {
//        return new InvalidMediaTypeFilter();
//    }
//
//    @Bean
//    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
//    public CorsFilter corsFilter() {
//        return new CorsFilter();
//    }
//
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Collections.singletonList("*"));
//        configuration.setAllowedMethods(Collections.singletonList("*"));
//        configuration.setAllowedHeaders(Arrays.asList("X-FHIR-Starter", "authorization", "Prefer", "Origin", "Accept", "X-Requested-With", "Content-Type", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
////    @Override
////    protected void configure(AuthenticationManagerBuilder auth)
////            throws Exception {
////        auth.inMemoryAuthentication()
////                .withUser("john").password("123").roles("USER");
////    }
//
////    @Autowired
////    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
////        auth
////                .inMemoryAuthentication()
////                .withUser("user").password("password").roles("USER");
////    }
//
////    @Override
////    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
////        oauthServer.tokenKeyAccess("isAnonymous() || hasAuthority('ROLE_TRUSTED_CLIENT')").checkTokenAccess(
////                "hasAuthority('ROLE_TRUSTED_CLIENT')");
////    }
//
////    @Override
////    @Bean
////    public AuthenticationManager authenticationManagerBean()
////            throws Exception {
////        return super.authenticationManagerBean();
////    }
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // add the corsFilter before the ChannelProcessingFilter
//        CorsFilter corsFilter = corsFilter();
//        http.addFilterBefore(corsFilter, ChannelProcessingFilter.class);
//
//        // add the invalidMediaTypeFilter before the CorsFilter
//        // (otherwise the CorsFilter will throw an exception for invalid media type)
//        InvalidMediaTypeFilter invalidMediaTypeFilter = invalidMediaTypeFilter();
//        http.addFilterBefore(invalidMediaTypeFilter, CorsFilter.class);
//
//        http
//                .authorizeRequests()
//                .anyRequest().permitAll()
//                .and()
//                .csrf().disable()
////                .antMatchers("/api").permitAll()
////                .anyRequest().authenticated()
////                .and()
//                .formLogin().disable()
//                .logout().disable();
//    }
//
////    @Bean
////    public TokenStore tokenStore() {
////        return new InMemoryTokenStore();
////    }
////
////    @Primary
////    @Bean
////    public RemoteTokenServices tokenService() {
////        RemoteTokenServices tokenService = new RemoteTokenServices();
////        tokenService.setCheckTokenEndpointUrl(checkTokenEndpointUrl);
//////                "http://localhost:8080/spring-security-oauth-server/oauth/check_token");
////        tokenService.setClientId(adminClientId);
////        tokenService.setClientSecret(adminClientSecret);
////        return tokenService;
////    }
//
//}