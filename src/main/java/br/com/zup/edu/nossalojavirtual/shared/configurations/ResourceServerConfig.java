package br.com.zup.edu.nossalojavirtual.shared.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {
    /**
     * Override this method to configure the {@link HttpSecurity}. Typically subclasses
     * should not invoke this method by calling super as it may override their
     * configuration. The default configuration is:
     *
     * <pre>
     * http.authorizeRequests().anyRequest().authenticated().and().formLogin().and().httpBasic();
     * </pre>
     * <p>
     * Any endpoint that requires defense against common vulnerabilities can be specified
     * here, including public ones. See {@link HttpSecurity#authorizeRequests} and the
     * `permitAll()` authorization rule for more details on public endpoints.
     *
     * @param http the {@link HttpSecurity} to modify
     * @throws Exception if an error occurs
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
            .and()
                .csrf().disable()
                .httpBasic().disable()
                .rememberMe().disable()
                .logout().disable()
                .formLogin().disable()
                .headers().frameOptions().deny().and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/categories").hasAuthority("SCOPE_categories:write")
                .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                .antMatchers(HttpMethod.POST, "/api/purchases/confirm-payment").hasAuthority("SCOPE_purchases:write")
                .antMatchers(HttpMethod.POST, "/api/purchase").hasAuthority("SCOPE_purchases:write")
                .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_products:write")
                .antMatchers(HttpMethod.GET, "/api/products/**").hasAuthority("SCOPE_products:read")
                .antMatchers(HttpMethod.POST, "/api/opinions").hasAuthority("SCOPE_opinions:write")
                .antMatchers(HttpMethod.POST, "/api/products/**/questions").hasAuthority("SCOPE_questions:write")
                .anyRequest().authenticated()
            .and()
                .oauth2ResourceServer().jwt((jwt) -> jwt.jwkSetUri("http://localhost:18080/realms/loja-virtual/protocol/openid-connect/certs"))
        ;
    }
}
