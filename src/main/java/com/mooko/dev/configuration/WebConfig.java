package com.mooko.dev.configuration;

import com.mooko.dev.converter.MultipartFileToFileConverter;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new MultipartFileToFileConverter());
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public GrantedAuthoritiesMapper grantedAuthoritiesMapper(){
        return new NullAuthoritiesMapper();
    }


}
