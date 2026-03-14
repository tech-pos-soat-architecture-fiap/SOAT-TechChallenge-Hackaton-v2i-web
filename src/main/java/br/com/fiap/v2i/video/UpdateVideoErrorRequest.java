package br.com.fiap.v2i.video;

public class UpdateVideoErrorRequest {
    private String videoHash;
    private String errorMessage;

    public UpdateVideoErrorRequest() {
    }

    public UpdateVideoErrorRequest(String videoHash, String errorMessage) {
        this.videoHash = videoHash;
        this.errorMessage = errorMessage;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
