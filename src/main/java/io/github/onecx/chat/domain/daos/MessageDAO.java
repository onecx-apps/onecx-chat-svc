package io.github.onecx.chat.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;

import io.github.onecx.chat.domain.models.Message;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MessageDAO extends AbstractDAO<Message> {

    public enum ErrorKeys {

        ERROR_CREATE_MESSAGE,
    }
}
