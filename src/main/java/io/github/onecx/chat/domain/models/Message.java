package io.github.onecx.chat.domain.models;

import static jakarta.persistence.FetchType.LAZY;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MESSAGE")
public class Message extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "TEXT")
    private String text;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "RELIABILITY")
    private Float reliability;

    @Column(name = "APP_ID")
    private String appId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "CHAT_ID")
    private Chat chat;

    public enum MessageType {
        SYSTEM,
        HUMAN,
        ASSISTANT,
    }

}
