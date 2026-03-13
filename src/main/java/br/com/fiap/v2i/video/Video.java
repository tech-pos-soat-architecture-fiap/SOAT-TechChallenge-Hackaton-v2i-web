package br.com.fiap.v2i.video;

import br.com.fiap.v2i.user.User;
import jakarta.persistence.*;

@Entity
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;
    private String fileName;
    private String fileHash;

    @Enumerated(EnumType.STRING)
    private VideoStatus status;

    @Deprecated
    public Video() {
    }

    private Video(User user, String fileName, String fileHash, VideoStatus status) {
        this.user = user;
        this.fileName = fileName;
        this.fileHash = fileHash;
        this.status = status;
    }

    public static Video createAsUploaded(User user, String fileName, String fileHash) {
        return new Video(user, fileName, fileHash, VideoStatus.UPLOADED);
    }
}
