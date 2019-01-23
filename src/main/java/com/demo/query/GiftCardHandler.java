package com.demo.query;

import com.demo.api.FindGiftCardQry;
import com.demo.api.GiftCardRecord;
import com.demo.api.IssuedEvt;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
class GiftCardHandler {

    private final GiftCardRepository giftCardRepository;
    private final QueryUpdateEmitter queryUpdateEmitter;

    @Autowired
    public GiftCardHandler(GiftCardRepository giftCardRepository, QueryUpdateEmitter queryUpdateEmitter) {
        this.giftCardRepository = giftCardRepository;
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    void on(IssuedEvt event) {
        /*
         * Update our read model by inserting the new card.
         */
        giftCardRepository.save(new GiftCardEntity(event.getId(), event.getAmount(), event.getAmount()));

        /* Send it to subscription queries of type FindGiftCardQry, but only if the card id matches. */
        queryUpdateEmitter.emit(FindGiftCardQry.class, findGiftCardQry -> Objects.equals(event.getId(), findGiftCardQry.getId()), new GiftCardRecord(event.getId(), event.getAmount(), event.getAmount()));

    }

    @QueryHandler
    GiftCardRecord handle(FindGiftCardQry query) {
        GiftCardEntity giftCardEntity = giftCardRepository.findById(query.getId()).orElse(new GiftCardEntity());
        return new GiftCardRecord(giftCardEntity.getId(), giftCardEntity.getInitialValue(), giftCardEntity.getRemainingValue());
    }
}
