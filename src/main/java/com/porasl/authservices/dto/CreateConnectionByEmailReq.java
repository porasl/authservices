package com.porasl.authservices.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateConnectionByEmailReq {
  @NotBlank @Email
  private String targetEmail;

  private String note; // optional
}