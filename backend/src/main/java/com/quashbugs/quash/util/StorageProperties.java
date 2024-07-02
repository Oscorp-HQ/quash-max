package com.quashbugs.quash.util;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@Configuration
public class StorageProperties {
    @Value("${gcp.credentials.project-id}")
    private String gcpProjectId;

    @Value("${gcp.credentials.client-email}")
    private String gcpClientEmail;

    @Value("${gcp.credentials.private-key}")
    private String gcpPrivateKey;

    @Value("${gcp.bucket.name}")
    private String gcpBucketName;

    @Value("${aws.access.key.id}")
    private String awsAccessKey;

    @Value("${aws.secret.access.key}")
    private String awsSecretKey;

    @Value("${aws.bucket.name}")
    private String awsBucketName;

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${azure.container.name:default}")
    private String azureContainerName;

    @Value("${azure.account.name:default}")
    private String azureAccountName;

    @Value("${azure.account.key:default}")
    private String azureAccountKey;
}
