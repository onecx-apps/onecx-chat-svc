package io.github.onecx.chat.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.daos.Page;
import org.tkit.quarkus.jpa.daos.PageResult;
import org.tkit.quarkus.jpa.exceptions.DAOException;
import org.tkit.quarkus.jpa.models.TraceableEntity_;
import org.tkit.quarkus.jpa.utils.QueryCriteriaUtil;

import io.github.onecx.chat.domain.criteria.ChatSearchCriteria;
import io.github.onecx.chat.domain.models.Chat;
import io.github.onecx.chat.domain.models.Chat_;

@ApplicationScoped
@Transactional(Transactional.TxType.NOT_SUPPORTED)
public class ChatDAO extends AbstractDAO<Chat> {

    // https://hibernate.atlassian.net/browse/HHH-16830#icft=HHH-16830
    @Override
    public Chat findById(Object id) throws DAOException {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Chat.class);
            var root = cq.from(Chat.class);
            cq.where(cb.equal(root.get(TraceableEntity_.ID), id));
            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception e) {
            throw new DAOException(ErrorKeys.FIND_ENTITY_BY_ID_FAILED, e, entityName, id);
        }
    }

    public PageResult<Chat> findChatsByCriteria(ChatSearchCriteria criteria) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Chat.class);
            var root = cq.from(Chat.class);

            if (criteria.getItemId() != null && !criteria.getItemId().isBlank()) {
                cq.where(cb.like(root.get(Chat_.itemId), QueryCriteriaUtil.wildcard(criteria.getItemId())));
            }

            return createPageQuery(cq, Page.of(criteria.getPageNumber(), criteria.getPageSize())).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_CHATS_BY_CRITERIA, ex);
        }
    }

    public PageResult<Chat> findAll(Integer pageNumber, Integer pageSize) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Chat.class);
            cq.from(Chat.class);
            return createPageQuery(cq, Page.of(pageNumber, pageSize)).getPageResult();
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_ALL_CHAT_PAGE, ex);
        }
    }

    public Chat findChatByItemId(String itemId) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = cb.createQuery(Chat.class);
            var root = cq.from(Chat.class);
            cq.where(cb.equal(root.get(Chat_.itemId), itemId));

            return this.getEntityManager().createQuery(cq).getSingleResult();

        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(ErrorKeys.ERROR_FIND_CHAT_BY_ITEM_ID, ex);
        }
    }

    public enum ErrorKeys {

        FIND_ENTITY_BY_ID_FAILED,
        ERROR_FIND_CHATS_BY_CRITERIA,
        ERROR_FIND_ALL_CHAT_PAGE,
        ERROR_FIND_CHAT_BY_ITEM_ID,
    }
}
