package com.porasl.authservices.dto;

//If you use a Record, you don't use .getPostId(), you use .postId()
public record DeleteRequest(String postId) {}