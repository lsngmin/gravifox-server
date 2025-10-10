package com.gravifox.tvb.infra.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.SesV2ClientBuilder;

@Configuration
public class SesConfig {

    @Bean
    @ConditionalOnProperty(name = "mail.sender", havingValue = "ses")
    public SesV2Client sesV2Client(
            @Value("${aws.region:ap-northeast-2}") String region,
            @Value("${aws.access-key-id:${aws.access-key:}}") String accessKeyId,
            @Value("${aws.secret-access-key:${aws.secret-key:}}") String secretAccessKey
    ) {
        SesV2ClientBuilder builder = SesV2Client.builder().region(Region.of(region));

        // application.yml에 키가 지정된 경우 정적 자격 증명 사용 (로컬 편의)
        if (accessKeyId != null && !accessKeyId.isBlank() && secretAccessKey != null && !secretAccessKey.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey))
            );
        }

        // 그렇지 않으면 기본 자격 증명 제공 체인 사용 (Env/IAM Role 등)
        return builder.build();
    }
}
