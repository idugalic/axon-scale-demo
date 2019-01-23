package com.demo.web;

import com.demo.api.FindGiftCardQry;
import com.demo.api.GiftCardRecord;
import com.demo.api.IssueCmd;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityLinks;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

/**
 * Repository REST Controller for handling 'commands' only
 * <p>
 * Sometimes you may want to write a custom handler for a specific resource. To take advantage of Spring Data REST’s settings, message converters, exception handling, and more, we use the @RepositoryRestController annotation instead of a standard Spring MVC @Controller or @RestController
 */
@RepositoryRestController
public class AxonJpaDemoRestController {

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;
    private final EntityLinks entityLinks;


    public AxonJpaDemoRestController(CommandGateway commandGateway, QueryGateway queryGateway, EntityLinks entityLinks) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
        this.entityLinks = entityLinks;
    }

    @RequestMapping(value = "/cards", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity cards(@RequestBody IssueRqst request) {

        final String giftCardId = UUID.randomUUID().toString();

        try (SubscriptionQueryResult<GiftCardRecord, GiftCardRecord> queryResult = queryGateway.subscriptionQuery(new FindGiftCardQry(giftCardId), ResponseTypes.instanceOf(GiftCardRecord.class), ResponseTypes.instanceOf(GiftCardRecord.class))) {
            commandGateway.sendAndWait(new IssueCmd(giftCardId, Integer.valueOf(request.getValue())));

            /* Returning the first update sent to our find card query. */
            GiftCardRecord giftCardRecord = queryResult.updates().blockFirst();
            return ResponseEntity.ok().body(giftCardRecord);
        }
    }
}