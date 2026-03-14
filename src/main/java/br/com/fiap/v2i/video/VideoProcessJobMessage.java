package br.com.fiap.v2i.video;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoProcessJobMessage {
    private String videoHash;
    private String userEmail;
    private String videoId;

    public VideoProcessJobMessage() {
    }

    public VideoProcessJobMessage(String videoHash, String userEmail, String videoId) {
        this.videoHash = videoHash;
        this.userEmail = userEmail;
        this.videoId = videoId;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    @Override
    public String toString() {
        return "VideoProcessJobMessage{" +
                "videoHash='" + videoHash + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", videoId='" + videoId + '\'' +
                '}';
    }
}