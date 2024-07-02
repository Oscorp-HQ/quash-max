package com.quashbugs.quash.config;

import com.quashbugs.quash.repo.*;
import com.quashbugs.quash.service.AwsStorageService;
import com.quashbugs.quash.service.AzureStorageService;
import com.quashbugs.quash.service.GcpStorageService;
import com.quashbugs.quash.service.StorageService;
import com.quashbugs.quash.util.StorageProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfiguration {

    @Value("${cloud.provider}")
    private String cloudProvider;

    @Bean
    public StorageService storageService(BugMediaRepository bugMediaRepository,
                                         GifMediaRepository gifMediaRepository,
                                         CrashLogRepository crashLogRepository,
                                         ApplicationRepository applicationRepository,
                                         ChatUploadRepository chatUploadRepository,
                                         StorageProperties storageProperties) {
        switch (cloudProvider.toLowerCase()) {
            case "gcp":
                return new GcpStorageService(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);
            case "aws":
                return new AwsStorageService(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);
            case "azure":
                return new AzureStorageService(bugMediaRepository, gifMediaRepository, crashLogRepository, applicationRepository, chatUploadRepository, storageProperties);
            default:
                throw new IllegalArgumentException("Invalid cloud provider: " + cloudProvider);
        }
    }
}
