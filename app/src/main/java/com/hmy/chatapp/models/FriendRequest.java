package com.hmy.chatapp.models;

public class FriendRequest {
    private String documentId, requestFromId, requestFromName, requestFromEmail, requestFromImage;

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getRequestFromId() {
        return requestFromId;
    }

    public void setRequestFromId(String requestFromId) {
        this.requestFromId = requestFromId;
    }

    public String getRequestFromName() {
        return requestFromName;
    }

    public void setRequestFromName(String requestFromName) {
        this.requestFromName = requestFromName;
    }

    public String getRequestFromEmail() {
        return requestFromEmail;
    }

    public void setRequestFromEmail(String requestFromEmail) {
        this.requestFromEmail = requestFromEmail;
    }

    public String getRequestFromImage() {
        return requestFromImage;
    }

    public void setRequestFromImage(String requestFromImage) {
        this.requestFromImage = requestFromImage;
    }
}
