package com.example.bookingtour.dtos.response.auth;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticationResponse {
   private String token;
   private String refreshToken;
   private boolean authenticated;
}
