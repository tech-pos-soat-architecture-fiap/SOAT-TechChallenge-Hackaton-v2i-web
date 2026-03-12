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
    private VideoStatus status;

    @Deprecated
    public Video() {
    }

    private Video(User user, String fileName, VideoStatus status) {
        this.user = user;
        this.fileName = fileName;
        this.status = status;
    }

    public static Video createAsUploaded(User user, String fileName) {
        return new Video(user, fileName, VideoStatus.UPLOADED);
    }
}
