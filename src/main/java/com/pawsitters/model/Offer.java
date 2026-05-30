package com.pawsitters.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Angebot (Offer).
 * Ein Gastgeber sendet ein Angebot zu einer Betreuungsanfrage.
 * Der Tierhalter kann genau ein Angebot annehmen; alle anderen
 * werden automatisch abgelehnt.
 */
@Entity
@Table(name = "offers")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "host_id", nullable = false)
    private Host host;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", nullable = false)
    private CareRequest request;

    @PositiveOrZero(message = "Preis darf nicht negativ sein")
    @NotNull
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status = OfferStatus.PENDING;

    public Offer() {}

    public Offer(Host host, CareRequest request, BigDecimal totalPrice, String message) {
        this.host = host;
        this.request = request;
        this.totalPrice = totalPrice;
        this.message = message;
        this.status = OfferStatus.PENDING;
    }

    // Getter und Setter

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Host getHost() { return host; }
    public void setHost(Host host) { this.host = host; }

    public CareRequest getRequest() { return request; }
    public void setRequest(CareRequest request) { this.request = request; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }
}
