package br.com.fiap.v2i.aws;

public class PresignedUrlResponse {
    private String uploadUrl;
    private String key;

    public PresignedUrlResponse(String uploadUrl, String key) {
        this.uploadUrl = uploadUrl;
        this.key = key;
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
}
