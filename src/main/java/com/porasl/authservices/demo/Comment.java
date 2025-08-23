package com.porasl.authservices.demo;

class Comment {
    private String author;
    private String message;
    private Long createdAt;

    public Comment() {}

    public Comment(String author, String message, Long createdAt) {
        this.author = author;
        this.message = message;
        this.createdAt = createdAt;
    }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
}