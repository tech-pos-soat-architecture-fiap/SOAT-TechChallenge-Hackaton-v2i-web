package br.com.fiap.v2i.video;

import jakarta.validation.constraints.NotBlank;

public class UploadConfirmRequest {
    @NotBlank
    private String fileHash;

    public UploadConfirmRequest() {
    }

    public UploadConfirmRequest(String fileHash) {
        this.fileHash = fileHash;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
}


