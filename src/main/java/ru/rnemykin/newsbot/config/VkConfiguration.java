package ru.rnemykin.newsbot.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.JdbcUsersConnectionRepository;
import org.springframework.social.vkontakte.connect.VKontakteConnectionFactory;

import javax.sql.DataSource;

@Configuration
public class VkConfiguration implements SocialConfigurer  {

    private final DataSource dataSource;

    @Autowired
    public VkConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer configurer, Environment env) {
        configurer.addConnectionFactory(
                new VKontakteConnectionFactory(env.getProperty("vkontakte.clientId"), env.getProperty("vkontakte.clientSecret"))
        );
    }

    @Override
    public org.springframework.social.UserIdSource getUserIdSource() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                throw new IllegalStateException("Unable to get a ConnectionRepository: no user signed in");
            }
            return authentication.getName();
        };
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new JdbcUsersConnectionRepository(dataSource, connectionFactoryLocator, Encryptors.noOpText());
    }


//    @Bean
//    @Scope(value = BeanDefinition.SCOPE_PROTOTYPE)
//    public VKontakte vkontakte(ConnectionRepository repository) {
//        Connection<VKontakte> connection = repository.findPrimaryConnection(VKontakte.class);
//        return connection != null ? connection.getApi() : null;
//    }


}
