package io.github.onecx.chat.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;

import io.github.onecx.chat.domain.models.Message;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class MessageDAO extends AbstractDAO<Message> {

    /*
     * @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
     * public Message addMessage(String chatId, Message message) {
     *
     * try {
     * Chat chat = this.findById(chatId);
     *
     * // Ensure the chat is not null
     * if (chat != null) {
     * // Add the new message to the chat's messages set
     * chat.getMessages().add(message);
     * this.update(chat);
     * }
     * return message;
     * } catch (NoResultException nre) {
     * return null;
     * } catch (Exception e) {
     * throw new DAOException(ErrorKeys.ERROR_CREATE_MESSAGE, e, entityName, chatId);
     * }
     *
     * }
     */

    public enum ErrorKeys {

        ERROR_CREATE_MESSAGE,
    }
}
