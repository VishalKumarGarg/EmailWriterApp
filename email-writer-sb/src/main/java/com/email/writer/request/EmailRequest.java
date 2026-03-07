package com.email.writer.request;

import lombok.Data;

@Data
public class EmailRequest {
	private String emailContent;
	private String tone;
}
