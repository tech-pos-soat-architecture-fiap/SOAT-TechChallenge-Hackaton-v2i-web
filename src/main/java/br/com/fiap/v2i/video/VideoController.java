package br.com.fiap.v2i.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserRepository;
import br.com.fiap.v2i.utils.NotFoundException;
import jakarta.validation.Valid;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.apache.logging.log4j.LogManager.getLogger;

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final RabbitTemplate rabbitTemplate;
    private static final Logger logger = getLogger(VideoController.class);

    public VideoController(S3Service s3Service, UserRepository userRepository, VideoRepository videoRepository, RabbitTemplate rabbitTemplate) {
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.rabbitTemplate = rabbitTemplate;
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

    /**
     * Confirma que o upload foi concluído pelo cliente e publica um job para o serviço de processamento.
     * Isso substitui o fluxo baseado em S3 event/Lambda.
     */
    @PostMapping("/upload/confirm")
    public ResponseEntity<Void> confirmUpload(@Valid @RequestBody UploadConfirmRequest request,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(NotFoundException::new);

        Video video = videoRepository.findByHash(request.getFileHash()).orElseThrow(NotFoundException::new);
        if (!video.getUser().getId().equals(user.getId())) {
            throw new NotFoundException();
        }

        String userEmail = user.getEmail();
        VideoProcessJobMessage job = new VideoProcessJobMessage(video.getFileHash(), userEmail, video.getId().toString());

        // Exchange/route do processing
        logger.info("About to publish job: {}", job);
        rabbitTemplate.convertAndSend(QueueConfig.EXCHANGE_NAME, QueueConfig.ROUTING_KEY, job);
        logger.info("Published job to exchange={} rk={}", QueueConfig.EXCHANGE_NAME, QueueConfig.ROUTING_KEY);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/processing")
    public ResponseEntity<Void> markAsProcessing(@RequestBody String videoHash) {
        Video video = videoRepository.findByHash(videoHash).orElseThrow(NotFoundException::new);
        video.markAsProcessing();
        videoRepository.save(video);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/complete")
    public ResponseEntity<Void> markAsComplete(@RequestBody UpdateVideoRequest request) {
        Video video = videoRepository.findByHash(request.getVideoHash()).orElseThrow(NotFoundException::new);
        video.markAsComplete(request.getDownloadUrl());
        videoRepository.save(video);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/error")
    public ResponseEntity<Void> markAsError(@RequestBody UpdateVideoErrorRequest request) {
        Video video = videoRepository.findByHash(request.getVideoHash()).orElseThrow(NotFoundException::new);
        video.markAsError(request.getErrorMessage());
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

    @GetMapping("/download/{videoId}")
    public ResponseEntity<String> getDownloadUrl(@PathVariable Long videoId, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(NotFoundException::new);

        Video video = videoRepository.findById(videoId)
                .orElseThrow(NotFoundException::new);

        if (!video.getUser().getId().equals(user.getId())) {
            throw new NotFoundException();
        }

        if (video.getStatus() != VideoStatus.COMPLETED) {
            throw new NotFoundException();
        }

        // Gerar presigned GET do ZIP gerado pelo processing.
        String zipKey = outputZipKey(video.getFileHash());
        String presigned = s3Service.generatePresignedDownloadUrl(zipKey);

        return ResponseEntity.ok(presigned);
    }

    private String outputZipKey(String originalVideoKey) {
        String[] parts = originalVideoKey.split("/");
        if (parts.length >= 2) {
            return "outputs/" + parts[1] + "/frames.zip";
        }
        return "outputs/" + originalVideoKey.replace('/', '_') + "/frames.zip";
    }
}
