package org.logicahealth.sandboxmanagerapi.config;

import com.amazonaws.regions.Regions;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.logicahealth.sandboxmanagerapi.services.impl.SandboxServiceImpl;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableEncryptableProperties
public class SandboxManagerConfig {

    @Value("${hspc.platform.simultaneousSandboxCreationTasksLimit}")
    private int sandboxCloneTaskActiveThreadCount;

    private static Logger LOGGER = LoggerFactory.getLogger(SandboxServiceImpl.class.getName());

    @Bean
    public CloseableHttpClient httpClient() {
        SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).useSSL().build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            LOGGER.error("Error loading ssl context", e);
            throw new RuntimeException(e);
        }

        HttpClientBuilder builder = HttpClientBuilder.create();
        SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        builder.setSSLSocketFactory(sslConnectionFactory);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslConnectionFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
        builder.setConnectionManager(ccm);
        return builder.build();
    }

    @Bean
    public RestTemplate simpleRestTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ModelMapper modelMapper() { return new ModelMapper(); }

    @Bean(name = "sandboxSingleThreadedTaskExecutor")
    public Executor sandboxSingleThreadedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(sandboxCloneTaskActiveThreadCount);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

    @Bean(name = "sandboxDeleteHttpClient")
    @Scope("prototype")
    public CloseableHttpClient sandboxDeleteHttpClient() {
        return HttpClientBuilder.create().build();
    }

    @Bean
    public static AmazonS3 amazonS3Client() {
        return AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.US_EAST_1)
                                    .build();
    }

}
