package es.unizar.iaaa.geofencing.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import es.unizar.iaaa.geofencing.security.config.JwtAuthenticationEntryPoint;
import es.unizar.iaaa.geofencing.security.config.JwtAuthenticationTokenFilter;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint unauthorizedHandler;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers(GET, "/validatorUrl").permitAll()
                .antMatchers(GET, "/webjars/**").permitAll()
                .antMatchers(GET, "/images/*").permitAll()
                .antMatchers(GET, "/v2/*").permitAll()
                .antMatchers(GET, "/configuration/*").permitAll()
                .antMatchers(GET, "/swagger-resources").permitAll()
                .antMatchers(GET, "/error").permitAll()
                .antMatchers(GET, "/swagger-ui.html").permitAll()
                .antMatchers(OPTIONS, "/*/**").permitAll()
                .antMatchers(POST, "/api/users").permitAll()
                .antMatchers(POST, "/api/users/auth/**").permitAll()
                .antMatchers(GET, "/api/users/**").permitAll()
                .antMatchers(GET, "/api/geofences/area").permitAll()
                .antMatchers(GET, "/api/geofences/**").permitAll()
                .antMatchers(GET, "/api/rules/**").permitAll()
                .antMatchers(GET, "/api/notifications/**").permitAll()
                .antMatchers(GET, "/api/locations/**").permitAll()
                .antMatchers(POST, "/api/locations/**").permitAll()
                .anyRequest().authenticated();

        // Custom JWT based security filter
        http.addFilterBefore(authenticationTokenFilterBean(), UsernamePasswordAuthenticationFilter.class);

        // disable page caching
        http.headers().cacheControl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureAuthentication(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(this.userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public JwtAuthenticationTokenFilter authenticationTokenFilterBean() throws Exception {
        JwtAuthenticationTokenFilter authenticationTokenFilter = new JwtAuthenticationTokenFilter();
        authenticationTokenFilter.setAuthenticationManager(authenticationManagerBean());
        return authenticationTokenFilter;
    }
}