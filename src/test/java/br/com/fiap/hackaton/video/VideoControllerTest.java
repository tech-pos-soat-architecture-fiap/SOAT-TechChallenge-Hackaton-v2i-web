package br.com.fiap.hackaton.video;

import br.com.fiap.v2i.aws.PresignedUrlResponse;
import br.com.fiap.v2i.aws.S3Service;
import br.com.fiap.v2i.user.User;
import br.com.fiap.v2i.user.UserRepository;
import br.com.fiap.v2i.video.UploadInitiateRequest;
import br.com.fiap.v2i.video.VideoController;
import br.com.fiap.v2i.video.VideoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VideoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private S3Service s3Service;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private VideoController videoController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        HandlerMethodArgumentResolver mockAuthResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().isAssignableFrom(UserDetails.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return userDetails;
            }

        };

        mockMvc = MockMvcBuilders.standaloneSetup(videoController)
                .setCustomArgumentResolvers(mockAuthResolver)
                .setControllerAdvice(new Object() {
                    @ExceptionHandler(RuntimeException.class)
                    public ResponseEntity<String> handle(RuntimeException e) {
                        return ResponseEntity.status(500).body(e.getMessage());
                    }
                })
                .build();
    }

    @Test
    void initiateUpload_Success() throws Exception {
        UploadInitiateRequest request = new UploadInitiateRequest("video.mp4", "video/mp4", 1024L);
        PresignedUrlResponse response = new PresignedUrlResponse("http://url", "key", "fileHash");

        when(userDetails.getUsername()).thenReturn("userTest");
        when(s3Service.generatePresignedUrl(anyString(), anyString(), anyLong())).thenReturn(response);
        when(userRepository.findByUsername("userTest")).thenReturn(Optional.of(new User("userTest", "123")));

        mockMvc.perform(post("/api/video/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(videoRepository, times(1)).save(any());
    }

    @Test
    void initiateUpload_UserNotFound() throws Exception {
        UploadInitiateRequest request = new UploadInitiateRequest("video.mp4", "video/mp4", 1024L);

        when(userDetails.getUsername()).thenReturn("usuario_fantasma");
        when(userRepository.findByUsername("usuario_fantasma")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/video/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(videoRepository, never()).save(any());
    }

    @Test
    void initiateUpload_S3ServiceFailure() {
        UploadInitiateRequest request = new UploadInitiateRequest("video.mp4", "video/mp4", 1024L);

        when(s3Service.generatePresignedUrl(anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Erro ao conectar com AWS"));

        org.junit.jupiter.api.Assertions.assertThrows(jakarta.servlet.ServletException.class, () -> {
            mockMvc.perform(post("/api/video/upload")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));
        });

        verify(userRepository, never()).findByUsername(anyString());
        verify(videoRepository, never()).save(any());
    }
}