package io.github.onecx.chat.domain.models;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "CHAT")
@SuppressWarnings("java:S2160")
public class Chat extends TraceableEntity {

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private ChatType type;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "TOPIC")
    private String topic;

    @Column(name = "SUMMARY")
    private String summary;

    @Column(name = "APP_ID")
    private String appId;

    public enum ChatType {
        HUMAN_CHAT,
        AI_CHAT
    }

}
