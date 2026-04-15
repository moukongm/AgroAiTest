package com.main;

import com.agri.pest.client.model.response.MessageResponseDto;

public class MessageCommentResponseDto {
    String url;
    MessageResponseDto messageResponseDto;

    public MessageCommentResponseDto(MessageResponseDto messageResponseDto, String url) {
        this.messageResponseDto = messageResponseDto;
        this.url = url;
    }

    public MessageCommentResponseDto() {
    }

    public MessageResponseDto getMessageResponseDto() {
        return messageResponseDto;
    }

    public String getUrl() {
        return url;
    }
}
