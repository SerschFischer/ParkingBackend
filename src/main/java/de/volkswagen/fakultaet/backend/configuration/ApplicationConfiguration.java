package de.volkswagen.fakultaet.backend.configuration;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Configuration
public class ApplicationConfiguration {
    Environment environment;

    public ApplicationConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CloudBlobClient cloudBlobClient() throws URISyntaxException, InvalidKeyException {
        return CloudStorageAccount
                .parse(this.environment.getProperty("spring.cloud.azure.storage.blob.connection-string"))
                .createCloudBlobClient();
    }

    @Bean
    public CloudBlobContainer cloudBlobContainer() throws URISyntaxException, InvalidKeyException, StorageException {
        return cloudBlobClient()
                .getContainerReference(this.environment.getProperty("spring.cloud.azure.storage.blob.container-name"));
    }
}
