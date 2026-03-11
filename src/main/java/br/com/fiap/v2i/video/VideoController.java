package br.com.fiap.v2i.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final S3Service s3Service;
    private final VideoJobService videoJobService;

    public VideoController(S3Service s3Service, VideoJobService videoJobService) {
        this.s3Service = s3Service;
        this.videoJobService = videoJobService;
    }

    @PostMapping("/upload1")
    public ResponseEntity<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        try {
            String s3Key = s3Service.uploadFile(file);
            videoJobService.sendVideoProcessingRequest(s3Key);
            return ResponseEntity.accepted().body(Map.of("jobId", s3Key, "status", "PENDING"));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not save file"));
        }
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

//    @GetMapping("/status/{jobId}")
//    public ResponseEntity<VideoJob> getStatus(@PathVariable String jobId) {
//        VideoJob job = videoJobService.getJob(jobId);
//        if (job == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(job);
//    }
//
//    @GetMapping("/download/{jobId}")
//    public ResponseEntity<Resource> downloadResult(@PathVariable String jobId) {
//        VideoJob job = videoJobService.getJob(jobId);
//
//        if (job == null || job.getStatus() != JobStatus.COMPLETED) {
//            return ResponseEntity.badRequest().build();
//        }
//
//        File zipFile = new File(job.getResultPath());
//        if (!zipFile.exists()) {
//            return ResponseEntity.internalServerError().build();
//        }
//
//        Resource resource = new FileSystemResource(zipFile);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=frames.zip")
//                .contentLength(zipFile.length())
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .body(resource);
//    }
}
