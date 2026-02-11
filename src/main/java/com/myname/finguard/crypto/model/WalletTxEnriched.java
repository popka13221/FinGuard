package com.myname.finguard.crypto.model;

import com.myname.finguard.auth.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "wallet_tx_enriched")
public class WalletTxEnriched {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CryptoWallet wallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_tx_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private WalletTxRaw rawTx;

    @Column(name = "tx_hash", nullable = false, length = 128)
    private String txHash;

    @Column(name = "log_index", nullable = false)
    private long logIndex;

    @Column(name = "tx_at", nullable = false)
    private Instant txAt;

    @Column(length = 8)
    private String direction;

    @Column(name = "asset_code", length = 24)
    private String assetCode;

    @Column(precision = 38, scale = 18)
    private BigDecimal amount;

    @Column(name = "amount_usd", precision = 38, scale = 18)
    private BigDecimal amountUsd;

    @Column(length = 64)
    private String category;

    @Column(name = "counterparty_normalized", length = 160)
    private String counterpartyNormalized;

    @Column(name = "recurring_candidate", nullable = false)
    private boolean recurringCandidate;

    @Column(precision = 5, scale = 4)
    private BigDecimal confidence;

    @Column(nullable = false, length = 32)
    private String source;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CryptoWallet getWallet() {
        return wallet;
    }

    public void setWallet(CryptoWallet wallet) {
        this.wallet = wallet;
    }

    public WalletTxRaw getRawTx() {
        return rawTx;
    }

    public void setRawTx(WalletTxRaw rawTx) {
        this.rawTx = rawTx;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(long logIndex) {
        this.logIndex = logIndex;
    }

    public Instant getTxAt() {
        return txAt;
    }

    public void setTxAt(Instant txAt) {
        this.txAt = txAt;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmountUsd() {
        return amountUsd;
    }

    public void setAmountUsd(BigDecimal amountUsd) {
        this.amountUsd = amountUsd;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCounterpartyNormalized() {
        return counterpartyNormalized;
    }

    public void setCounterpartyNormalized(String counterpartyNormalized) {
        this.counterpartyNormalized = counterpartyNormalized;
    }

    public boolean isRecurringCandidate() {
        return recurringCandidate;
    }

    public void setRecurringCandidate(boolean recurringCandidate) {
        this.recurringCandidate = recurringCandidate;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public void setConfidence(BigDecimal confidence) {
        this.confidence = confidence;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
