package io.github.onecx.chat.domain.models;

import static jakarta.persistence.FetchType.LAZY;

import java.util.Set;

import jakarta.persistence.*;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "PARTICIPANT")
public class Participant extends TraceableEntity {

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "TYPE")
    @Enumerated(EnumType.STRING)
    private ParticipantType type;

    @Column(name = "USER_NAME")
    private String userName;

    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "CHAT_PARTICIPANT", joinColumns = @JoinColumn(name = "USER_ID"), inverseJoinColumns = @JoinColumn(name = "CHAT_ID"))
    private Set<Chat> chats;

    public enum ParticipantType {
        HUMAN,
        ASSISTANT,
    }

}
