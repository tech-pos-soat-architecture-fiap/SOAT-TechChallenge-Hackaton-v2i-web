package br.com.fiap.v2i.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final S3Service s3Service;

    public VideoController(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<PresignedUrlResponse> initiateUpload(@RequestBody UploadInitiateRequest request) {

        PresignedUrlResponse response = s3Service.generatePresignedUrl(
                request.getFilename(),
                request.getContentType(),
                request.getFileSize()
        );

        return ResponseEntity.ok(response);
    }
}
