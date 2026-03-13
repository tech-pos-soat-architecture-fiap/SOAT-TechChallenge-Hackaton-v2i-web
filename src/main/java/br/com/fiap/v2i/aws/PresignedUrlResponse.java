package br.com.fiap.v2i.aws;

public class PresignedUrlResponse {
    private String uploadUrl;
    private String key;
    private String fileHash;

    public PresignedUrlResponse(String uploadUrl, String key, String fileHash) {
        this.uploadUrl = uploadUrl;
        this.key = key;
        this.fileHash = fileHash;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public void setUploadUrl(String uploadUrl) {
        this.uploadUrl = uploadUrl;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }
}
