package br.com.fiap.v2i.video;

import br.com.fiap.v2i.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v WHERE v.fileHash = :fileHash")
    Optional<Video> findByHash(String fileHash);

    List<Video> findByUser(User user);
}
