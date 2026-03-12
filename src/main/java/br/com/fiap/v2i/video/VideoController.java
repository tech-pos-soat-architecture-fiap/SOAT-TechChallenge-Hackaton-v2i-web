package br.com.fiap.v2i.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserRepository;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<PresignedUrlResponse> initiateUpload(@RequestBody UploadInitiateRequest request, @AuthenticationPrincipal UserDetails userDetails) throws ChangeSetPersister.NotFoundException {

        PresignedUrlResponse response = s3Service.generatePresignedUrl(
                request.getFilename(),
                request.getContentType(),
                request.getFileSize()
        );

        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow(ChangeSetPersister.NotFoundException::new);

        Video video = Video.createAsUploaded(user, request.getFilename());
        videoRepository.save(video);

        return ResponseEntity.ok(response);
    }
}
