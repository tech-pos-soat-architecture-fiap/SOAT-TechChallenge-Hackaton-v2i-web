package br.com.fiap.v2i.video;

public class UpdateVideoRequest {
    private String videoHash;
    private String downloadUrl;

    public UpdateVideoRequest() {
    }

    public UpdateVideoRequest(String videoHash, String downloadUrl) {
        this.videoHash = videoHash;
        this.downloadUrl = downloadUrl;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}

