package com.demo.web;

import com.demo.api.FindGiftCardQry;
import com.demo.api.GiftCardRecord;
import com.demo.api.IssueCmd;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.UUID;

/**
 * Repository REST Controller for handling 'commands' only
 * <p>
 */
@Profile("command")
@RestController
public class AxonJpaDemoRestController {

    private static final Logger log = LoggerFactory.getLogger(AxonJpaDemoRestController.class);

    private final CommandGateway commandGateway;
    private final QueryGateway queryGateway;


    public AxonJpaDemoRestController(CommandGateway commandGateway, QueryGateway queryGateway) {
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @RequestMapping(value = "/commandcards", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity cards(@RequestBody IssueRqst request) {

        final String giftCardId = UUID.randomUUID().toString();

        try (SubscriptionQueryResult<GiftCardRecord, GiftCardRecord> queryResult = queryGateway.subscriptionQuery(new FindGiftCardQry(giftCardId), ResponseTypes.instanceOf(GiftCardRecord.class), ResponseTypes.instanceOf(GiftCardRecord.class))) {
            commandGateway.sendAndWait(new IssueCmd(giftCardId, Integer.valueOf(request.getValue())));

            /* Returning the first update sent to our find card query. */
            GiftCardRecord giftCardRecord = queryResult.updates().blockFirst(Duration.ofSeconds(5));

            return ResponseEntity.ok().body(giftCardRecord);
        } catch (Exception ex) {
            log.error("Subscribing to query error !");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
