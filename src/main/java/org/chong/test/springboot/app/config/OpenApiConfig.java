package org.chong.test.springboot.app.config;

//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Configuration;

@Configuration
/*@OpenAPIDefinition(
        info = @Info(
                title = "Banco Cuenta CRUD",
                version = "1.0.0",
                description = "This is a CRUD for management of Banco and Cuenta"
        )
)*/
public class OpenApiConfig {
    public OpenAPI api() {
        return new OpenAPI()
                .info(new Info()
                        .title("Banco Cuenta CRUD")
                        .version("1.0.0")
                        .description("This is a CRUD for management of Banco and Cuenta")
                        .contact(new Contact()
                                .name("Carlos Chong Zapata")
                                .url("URL")
                                .email("carloschong10@gmail.com")));
    }
}
