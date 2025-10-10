package com.gravifox.tvb.domain.analysis.service;

public interface WebClientService {
    String sendImageToAIServer(String uuid);
    String sendVideoToAIServer(String uuid);

}
