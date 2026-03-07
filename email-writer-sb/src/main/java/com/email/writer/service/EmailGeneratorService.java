package com.email.writer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.email.writer.request.EmailRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EmailGeneratorService {
	
	private final WebClient client;
	private final String apiKey;
	
	
	public EmailGeneratorService(WebClient.Builder builder,@Value("${gemini.api.url}") String baseUrlGet,@Value("${gemini.api.key}") String geminiApiKey) {
		this.client = builder.baseUrl(baseUrlGet).build();
		this.apiKey = geminiApiKey;
	}
	private String buildPrompt(EmailRequest emailRequest) {
		StringBuilder prompt=new StringBuilder();
		prompt.append("Generate a email reply for the following email: \n");
		prompt.append("Original Email: \n").append(emailRequest.getEmailContent());
		if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()) {
			prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.");
//			Use a professional tone. 
//			Use a friendly tone. 
//			Use a casual tone. 
		}
		
		System.out.println("Created prompt in 1");
		return prompt.toString();
//		if tone is mention
//		Generate a professional email reply for the following email: Use a professional tone. Original Email: \n mail body
//		Generate a professional email reply for the following email: Use a friendly tone. Original Email: \n mail body
//		Generate a professional email reply for the following email: Use a casual tone. Original Email: \n mail body
//		else tone is not mention
//		Generate a professional email reply for the following email: Original Email: \n mail body
	}
	private String jsonFormat(String prompt) {
		String jsonBody=String.format("""
				{
			    "contents": [
			      {
			        "parts": [
			          {
			            "text": "%s"
			          }
			        ]
			      }
			    ]
			  }""",prompt);
		System.out.println("Create the jsonFromat");
		return jsonBody;
		
	}
	private String extractResponseContent(String res) {
		
		try {
			ObjectMapper mapper =new ObjectMapper();
			JsonNode root=mapper.readTree(res);
			System.out.println("Send the response");
			return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();//JSON navigation.
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		System.out.println("OOPs error");
		return "Error";
	}
	public String generateEmailReply(EmailRequest emailRequest) {
//		Build prompt. 
		String prompt=buildPrompt(emailRequest);
//		prepare raw json body
		String reqBody=jsonFormat(prompt);
		System.out.println("Done");
//		send request
		String res=client.post()
				.uri(uriBuilder -> uriBuilder
						.path("/v1beta/models/gemini-2.5-flash:generateContent")
						.build())
				.header("x-goog-api-key", apiKey)
				.header("Content-Type", "application/json")
				.bodyValue(reqBody)
				.retrieve()
				.bodyToMono(String.class)
				.block();
		//		Extract response
		return extractResponseContent(res);
	}
	
	

	
	
}
