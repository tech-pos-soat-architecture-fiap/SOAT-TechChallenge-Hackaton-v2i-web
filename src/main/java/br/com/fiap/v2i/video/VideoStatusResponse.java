package br.com.fiap.v2i.video;

public record VideoStatusResponse(
        Long id,
        String fileName,
        VideoStatus status,
        String fileHash,
        String downloadUrl,
        String errorMessage
) {
    public static VideoStatusResponse fromEntity(Video video) {
        return new VideoStatusResponse(
                video.getId(),
                video.getFileName(),
                video.getStatus(),
                video.getFileHash(),
                video.getDownloadUrl(),
                video.getErrorMessage()
        );
    }
}