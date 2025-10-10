package com.gravifox.domain.issue.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravifox.domain.issue.dto.GithubIssue;
import com.gravifox.domain.issue.service.GitHubDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class GitHubDataServiceImpl implements GitHubDataService {

    @Value("${git-hub.repo.tvb}") private String tvbBaseUrl;
    @Value("${git-hub.repo.tvb-ai}") private String tvbAiBaseUrl;
    @Value("${git-hub.repo.tvb-front}") private String tvbFrontBaseUrl;
    @Value("${git-hub.repo.token}") private String token;


    @Override
    public String fetchIssueData()  {
        WebClient webClient = initWebClient(tvbBaseUrl);
        WebClient webClient_front = initWebClient(tvbFrontBaseUrl);

        String response = webClient.get()
                .uri("?labels=🐞 BugFix")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        String response_front = webClient_front.get()
                .uri("?labels=🐞 BugFix")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<GithubIssue> issues_front = objectMapper.readValue(response_front, new TypeReference<>() {});
            List<GithubIssue> issues = objectMapper.readValue(response, new TypeReference<>() {});

            List<GithubIssue> mergedList = Stream.concat(issues_front.stream(), issues.stream())
                    .distinct()
                    .toList();

            log.info(mergedList.toString());
            return objectMapper.writeValueAsString(mergedList);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());

        }
        return "";
    }

//    public String sendImageToAIServer(String uuid) {
//        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
//        try {
//            InputStream is = Files.newInputStream(findFilePath(uuid));
//            byte[] imageBytes = is.readAllBytes();  // Java 9 이상
//            ByteArrayResource resource = new ByteArrayResource(imageBytes);
//            bodyBuilder.part("file", resource)
//                    .filename("detectedObject.jpg")
//                    .contentType(MediaType.IMAGE_JPEG);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.info("{}", fastApiUrl);
//        WebClient webClient = WebClient.builder()
//                .baseUrl(fastApiUrl)
//                .build();
//
//        String analyzeResult = webClient.post()
//                .uri("/upload/")
//                .contentType(MediaType.MULTIPART_FORM_DATA)
//                .bodyValue(bodyBuilder.build())
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//        log.info("{}", analyzeResult);
//        return analyzeResult;
//    }
//    private Path findFilePath(String uuid) {
//        Path dirPath = Paths.get(uploadPath);
//        try (Stream<Path> stream = Files.list(dirPath)) {
//            Optional<Path> foundPath = stream
//                    .filter(Files::isRegularFile)
//                    .filter(path -> path.getFileName().toString().equals(uuid))
//                    .findFirst();
//            Path filePath = null;
//            if (foundPath.isPresent()) {
//                filePath = foundPath.get();
//                log.info("{}", filePath);
//                return filePath;
//            }
//            //TODO Need Throw Exception
//            log.info("Failed to find file path {}", dirPath);
//            return null;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        log.info("Failed to find file path2 {}", dirPath);
//        return null;
//
//    }

    private WebClient initWebClient(String baseUrl) {
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.set("Accept", "application/vnd.github+json");
                    httpHeaders.set("Authorization", token);
                    httpHeaders.set("X-GitHub-Api-Version", "2022-11-28");
                })
                .build();
        return webClient;
    }
}
