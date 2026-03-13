package br.com.fiap.v2i.video;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {

    @Query("SELECT v FROM Video v WHERE v.fileHash = :fileHash")
    Optional<Video> findByHash(String fileHash);
}
