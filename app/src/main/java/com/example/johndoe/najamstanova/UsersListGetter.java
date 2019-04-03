package com.example.johndoe.najamstanova;

public class UsersListGetter {

    private final String message_id;
    public final String sender_uid;
    public final String sender_name;
    public final String receiver_uid;
    public final String receiver_name;
    private final String message_body;
    private final String message_read;

    public UsersListGetter(String message_id, String sender_uid, String sender_name, String receiver_uid, String receiver_name, String message_body, String message_read) {
        this.message_id = message_id;
        this.sender_uid = sender_uid;
        this.sender_name = sender_name;
        this.receiver_uid = receiver_uid;
        this.receiver_name = receiver_name;
        this.message_body = message_body;
        this.message_read = message_read;
    }

    public String getMessageId() {
        return message_id;
    }

    public String getSenderUid() {
        return sender_uid;
    }

    public String getSenderName() {
        return sender_name;
    }

    public String getReceiverUid() {
        return receiver_uid;
    }

    public String getReceiverName() {
        return receiver_name;
    }

    public String getMessageBody() {
        return message_body;
    }

    public String getMessageRead() {
        return message_read;
    }




}