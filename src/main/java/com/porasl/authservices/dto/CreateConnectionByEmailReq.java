package com.porasl.authservices.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateConnectionByEmailReq {
	@NotBlank
	@Email
	private String targetEmail;

	private String notes;

	public CreateConnectionByEmailReq() {}

	public String getTargetEmail() { return targetEmail; }
	public void setTargetEmail(String targetEmail) { this.targetEmail = targetEmail; }
	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
}