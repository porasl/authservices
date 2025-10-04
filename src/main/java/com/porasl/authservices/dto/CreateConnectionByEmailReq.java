package com.porasl.authservices.dto;

import lombok.Data;

@Data
public class CreateConnectionByEmailReq {
  @jakarta.validation.constraints.Email
  @jakarta.validation.constraints.NotBlank
  private String targetEmail;
}
