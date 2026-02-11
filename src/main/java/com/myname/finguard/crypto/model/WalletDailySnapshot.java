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
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "wallet_daily_snapshots")
public class WalletDailySnapshot {

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

    @Column(name = "snapshot_day", nullable = false)
    private LocalDate day;

    @Column(name = "portfolio_usd", precision = 38, scale = 18)
    private BigDecimal portfolioUsd;

    @Column(name = "inflow_usd", precision = 38, scale = 18)
    private BigDecimal inflowUsd;

    @Column(name = "outflow_usd", precision = 38, scale = 18)
    private BigDecimal outflowUsd;

    @Column(name = "net_flow_usd", precision = 38, scale = 18)
    private BigDecimal netFlowUsd;

    @Column(name = "pnl_usd", precision = 38, scale = 18)
    private BigDecimal pnlUsd;

    @Column(name = "pnl_pct", precision = 12, scale = 6)
    private BigDecimal pnlPct;

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

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public BigDecimal getPortfolioUsd() {
        return portfolioUsd;
    }

    public void setPortfolioUsd(BigDecimal portfolioUsd) {
        this.portfolioUsd = portfolioUsd;
    }

    public BigDecimal getInflowUsd() {
        return inflowUsd;
    }

    public void setInflowUsd(BigDecimal inflowUsd) {
        this.inflowUsd = inflowUsd;
    }

    public BigDecimal getOutflowUsd() {
        return outflowUsd;
    }

    public void setOutflowUsd(BigDecimal outflowUsd) {
        this.outflowUsd = outflowUsd;
    }

    public BigDecimal getNetFlowUsd() {
        return netFlowUsd;
    }

    public void setNetFlowUsd(BigDecimal netFlowUsd) {
        this.netFlowUsd = netFlowUsd;
    }

    public BigDecimal getPnlUsd() {
        return pnlUsd;
    }

    public void setPnlUsd(BigDecimal pnlUsd) {
        this.pnlUsd = pnlUsd;
    }

    public BigDecimal getPnlPct() {
        return pnlPct;
    }

    public void setPnlPct(BigDecimal pnlPct) {
        this.pnlPct = pnlPct;
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
