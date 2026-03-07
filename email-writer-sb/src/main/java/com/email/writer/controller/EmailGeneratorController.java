package com.email.writer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.email.writer.request.EmailRequest;
import com.email.writer.service.EmailGeneratorService;
import com.email.writer.service.EmailGeneratorService2;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class EmailGeneratorController {
	private final EmailGeneratorService emailGeneratorService;
	@PostMapping("/generator1")
	public ResponseEntity<String> generatorEmail(@RequestBody EmailRequest emailRequest){
		String resp=emailGeneratorService.generateEmailReply(emailRequest); 
		System.out.println("Running 1");
		return ResponseEntity.ok(resp);
	}
	private final EmailGeneratorService2 emailGeneratorService2;
	@PostMapping("/generator2")
	public ResponseEntity<String> generatorEmail2(@RequestBody EmailRequest emailRequest){
		String resp=emailGeneratorService2.generateEmailReply(emailRequest);
		System.out.println("Running 2");
		return ResponseEntity.ok(resp);
	}
	
}
