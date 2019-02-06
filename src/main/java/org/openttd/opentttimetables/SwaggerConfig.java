package org.openttd.opentttimetables;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {                                    
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
          .select()                                  
          .apis(RequestHandlerSelectors.basePackage("org.openttd.opentttimetables.rest"))
          .paths((s) -> s != null && (s.startsWith("/destination")
                  || s.startsWith("/scheduled-dispatch")
                  || s.startsWith("/stats")
                  || s.startsWith("/timetable"))
          )
          .build();                                           
    }
}