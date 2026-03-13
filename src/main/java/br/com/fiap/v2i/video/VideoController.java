package br.com.fiap.v2i.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserRepository;
import br.com.fiap.v2i.utils.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    public VideoController(S3Service s3Service, UserRepository userRepository, VideoRepository videoRepository) {
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<PresignedUrlResponse> initiateUpload(@RequestBody UploadInitiateRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        PresignedUrlResponse response = s3Service.generatePresignedUrl(
                request.getFilename(),
                request.getContentType(),
                request.getFileSize()
        );

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(NotFoundException::new);

        Video video = Video.createAsUploaded(user, request.getFilename(), response.getKey());
        videoRepository.save(video);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/processing")
    public ResponseEntity<Void> markAsProcessing(@RequestBody String videoHash) {
        Video video = videoRepository.findByHash(videoHash).orElseThrow(NotFoundException::new);
        video.markAsProcessing();
        videoRepository.save(video);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> markAsComplete(@RequestBody String videoHash) {
        Video video = videoRepository.findByHash(videoHash).orElseThrow(NotFoundException::new);
        video.markAsComplete();
        videoRepository.save(video);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/error")
    public ResponseEntity<Void> markAsError(@RequestBody String videoHash) {
        Video video = videoRepository.findByHash(videoHash).orElseThrow(NotFoundException::new);
        video.markAsError();
        videoRepository.save(video);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    public ResponseEntity<List<VideoStatusResponse>> listMyVideos(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(NotFoundException::new);

        List<VideoStatusResponse> videos = videoRepository.findByUser(user)
                .stream()
                .map(VideoStatusResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(videos);
    }
}
