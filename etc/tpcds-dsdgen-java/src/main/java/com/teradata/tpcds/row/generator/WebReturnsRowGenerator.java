/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.teradata.tpcds.row.generator;

import com.google.common.collect.ImmutableList;
import com.teradata.tpcds.Scaling;
import com.teradata.tpcds.Session;
import com.teradata.tpcds.row.WebReturnsRow;
import com.teradata.tpcds.row.WebSalesRow;
import com.teradata.tpcds.type.Pricing;

import static com.teradata.tpcds.JoinKeyUtils.generateJoinKey;
import static com.teradata.tpcds.Nulls.createNullBitMap;
import static com.teradata.tpcds.Table.*;
import static com.teradata.tpcds.generator.WebReturnsGeneratorColumn.*;
import static com.teradata.tpcds.random.RandomValueGenerator.generateUniformRandomInt;
import static com.teradata.tpcds.type.Pricing.generatePricingForReturnsTable;
import static java.util.Collections.emptyList;

public class WebReturnsRowGenerator
        extends AbstractRowGenerator {
    public WebReturnsRowGenerator() {
        super(WEB_RETURNS);
    }

    @Override
    public RowGeneratorResult generateRowAndChildRows(long rowNumber, Session session, RowGenerator parentRowGenerator, RowGenerator childRowGenerator) {
        RowGeneratorResult salesAndReturnsResult = parentRowGenerator.generateRowAndChildRows(rowNumber, session, null, this);
        if (salesAndReturnsResult.getRowAndChildRows().size() == 2) {
            return new RowGeneratorResult(ImmutableList.of(salesAndReturnsResult.getRowAndChildRows().get(1)), salesAndReturnsResult.shouldEndRow());
        } else {
            return new RowGeneratorResult(emptyList(), salesAndReturnsResult.shouldEndRow());  // no return occurred for given sale
        }
    }

    public WebReturnsRow generateRow(Session session, WebSalesRow salesRow) {
        long nullBitMap = createNullBitMap(WEB_RETURNS, getRandomNumberStream(WR_NULLS));

        // fields taken from the original sale
        long wrItemSk = salesRow.getWsItemSk();
        long wrOrderNumber = salesRow.getWsOrderNumber();
        long wrWebPageSk = salesRow.getWsWebPageSk();

        // remaining fields are specific to this return
        Scaling scaling = session.getScaling();
        long wrReturnedDateSk = generateJoinKey(WR_RETURNED_DATE_SK, getRandomNumberStream(WR_RETURNED_DATE_SK), DATE_DIM, salesRow.getWsShipDateSk(), scaling);
        long wrReturnedTimeSk = generateJoinKey(WR_RETURNED_TIME_SK, getRandomNumberStream(WR_RETURNED_TIME_SK), TIME_DIM, 1, scaling);

        // items are usually returned to the people they were shipped to, but sometimes not
        long wrRefundedCustomerSk = generateJoinKey(WR_REFUNDED_CUSTOMER_SK, getRandomNumberStream(WR_REFUNDED_CUSTOMER_SK), CUSTOMER, 1, scaling);
        long wrRefundedCdemoSk = generateJoinKey(WR_REFUNDED_CDEMO_SK, getRandomNumberStream(WR_REFUNDED_CDEMO_SK), CUSTOMER_DEMOGRAPHICS, 1, scaling);
        long wrRefundedHdemoSk = generateJoinKey(WR_REFUNDED_HDEMO_SK, getRandomNumberStream(WR_REFUNDED_HDEMO_SK), HOUSEHOLD_DEMOGRAPHICS, 1, scaling);
        long wrRefundedAddrSk = generateJoinKey(WR_REFUNDED_ADDR_SK, getRandomNumberStream(WR_REFUNDED_ADDR_SK), CUSTOMER_ADDRESS, 1, scaling);
        if (generateUniformRandomInt(0, 99, getRandomNumberStream(WR_RETURNING_CUSTOMER_SK)) < WebSalesRowGenerator.GIFT_PERCENTAGE) {
            wrRefundedCustomerSk = salesRow.getWsShipCustomerSk();
            wrRefundedCdemoSk = salesRow.getWsShipCdemoSk();
            wrRefundedHdemoSk = salesRow.getWsShipHdemoSk();
            wrRefundedAddrSk = salesRow.getWsShipAddrSk();
        }

        long wrReturningCustomerSk = wrRefundedCustomerSk;
        long wrReturningCdemoSk = wrRefundedCdemoSk;
        long wrReturningHdemoSk = wrRefundedHdemoSk;
        long wrReturningAddrSk = wrRefundedAddrSk;

        long wrReasonSk = generateJoinKey(WR_REASON_SK, getRandomNumberStream(WR_REASON_SK), REASON, 1, scaling);
        int quantity = generateUniformRandomInt(1, salesRow.getWsPricing().getQuantity(), getRandomNumberStream(WR_PRICING));
        Pricing wrPricing = generatePricingForReturnsTable(WR_PRICING, getRandomNumberStream(WR_PRICING), quantity, salesRow.getWsPricing());

        return new WebReturnsRow(nullBitMap,
                wrReturnedDateSk,
                wrReturnedTimeSk,
                wrItemSk,
                wrRefundedCustomerSk,
                wrRefundedCdemoSk,
                wrRefundedHdemoSk,
                wrRefundedAddrSk,
                wrReturningCustomerSk,
                wrReturningCdemoSk,
                wrReturningHdemoSk,
                wrReturningAddrSk,
                wrWebPageSk,
                wrReasonSk,
                wrOrderNumber,
                wrPricing);
    }
}
