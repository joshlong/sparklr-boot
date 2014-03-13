package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.OAuth2ResourceServerConfigurer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


/***
 * this whole example was lifted wholesale from Dr. Dave Syer's epic
 * @author Dave Syer
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@Import(RepositoryRestMvcConfiguration.class)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    public static final String CRM_RESOURCE_ID = "crm";

    @Configuration
    @EnableResourceServer
    protected static class ResourceServer extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http
                    .requestMatchers().antMatchers("/*", "/admin/beans").and()
                    .authorizeRequests()
                    .anyRequest().access("#oauth2.hasScope('read')");
            // @formatter:on
        }

        @Override
        public void configure(OAuth2ResourceServerConfigurer resources) throws Exception {
            resources.resourceId(CRM_RESOURCE_ID);
        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

        @Autowired
        private AuthenticationManager authenticationManager;

        @Override
        public void configure(OAuth2AuthorizationServerConfigurer oauthServer) throws Exception {
            oauthServer.authenticationManager(authenticationManager);
        }

        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            // @formatter:off
            clients.inMemory()
                    .withClient("ios-crm")
                    .authorizedGrantTypes("client_credentials", "password")
                    .authorities("ROLE_CLIENT")
                    .scopes("read")
                    .resourceIds(CRM_RESOURCE_ID)
                    .secret("secret");
            // @formatter:on
        }

    }

}


@RepositoryRestResource
interface CustomerRepository extends JpaRepository<Customer, Long> {
}

@Entity
class Customer {

    @Id
    @GeneratedValue
    private Long id;
    private String firstName, lastName;


    Customer() {
    } // for JPA

    public Customer(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }
}
