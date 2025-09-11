package io.github.tony8864.dto;

public class ChatMessageDto {
    private String id;
    private String chatId;
    private String senderId;
    private String content;
    private String timestamp;

    public ChatMessageDto() {
    }

    public ChatMessageDto(String id, String chatId, String senderId, String content, String timestamp) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }
}