package com.nana.nana.domain.feature.auth.kcb

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun configureViewResolvers(registry: ViewResolverRegistry) {
        registry.jsp().prefix("/WEB-INF/views/").suffix(".jsp")
    }
}