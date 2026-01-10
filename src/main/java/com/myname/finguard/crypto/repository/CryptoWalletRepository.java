package com.myname.finguard.crypto.repository;

import com.myname.finguard.crypto.model.CryptoNetwork;
import com.myname.finguard.crypto.model.CryptoWallet;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
    List<CryptoWallet> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndArchivedFalse(Long userId);

    Optional<CryptoWallet> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndNetworkAndAddressNormalized(Long userId, CryptoNetwork network, String addressNormalized);

    Optional<CryptoWallet> findByUserIdAndNetworkAndAddressNormalized(Long userId, CryptoNetwork network, String addressNormalized);
}
