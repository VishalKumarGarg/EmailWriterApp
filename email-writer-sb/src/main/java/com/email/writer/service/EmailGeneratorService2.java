package com.email.writer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.email.writer.request.EmailRequest;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;


@Service
public class EmailGeneratorService2 {
	private final Client client;
	public EmailGeneratorService2(@Value("${gemini.api.key}") String apiKey) {
        this.client = Client.builder()
                .apiKey(apiKey)
                .build();
    }
	private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a email reply without subject for the following email:\n");
        prompt.append("Original Email:\n").append(emailRequest.getEmailContent()).append("\n\n");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
        }

        return prompt.toString();
    }
	public String generateEmailReply(EmailRequest emailRequest) {
		String prompt=buildPrompt(emailRequest);
		GenerateContentResponse response=client.models.generateContent("gemini-2.5-flash",prompt,null);
		return response.text();
	}
}
