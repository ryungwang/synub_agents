package com.company.aidevstaff.aiproxy.infrastructure;

import com.company.aidevstaff.aiproxy.domain.ProxyToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProxyTokenRepository extends JpaRepository<ProxyToken, String> {
    Optional<ProxyToken> findByTokenHash(String tokenHash);
}
