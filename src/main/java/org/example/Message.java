package org.example;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("sender_id")
    private String senderId;

    @JsonProperty("recipient_id")
    private String recipientId;

    @JsonProperty("message")
    private String message;

    @JsonProperty("is_group_message")
    private boolean isGroupMessage;

    // Constructors, getters, and setters
    public Message() {
    }

    public Message(String senderId, String recipientId, String message, boolean isGroupMessage) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.message = message;
        this.isGroupMessage = isGroupMessage;
    }

    // Getters and setters
    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isGroupMessage() {
        return isGroupMessage;
    }

    public void setGroupMessage(boolean groupMessage) {
        isGroupMessage = groupMessage;
    }
}
