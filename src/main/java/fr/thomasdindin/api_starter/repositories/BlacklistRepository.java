package fr.thomasdindin.api_starter.repositories;

import fr.thomasdindin.api_starter.entities.Blacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Integer> {
    Optional<Blacklist> findByAdresseIp(String adresseIp);

    @Query("SELECT MIN(b.dateBlocage) FROM Blacklist b")
    Optional<Instant> findMinDateBlocage();
}