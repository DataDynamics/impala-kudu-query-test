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

import com.teradata.tpcds.Session;
import com.teradata.tpcds.row.ShipModeRow;

import static com.teradata.tpcds.BusinessKeyGenerator.makeBusinessKey;
import static com.teradata.tpcds.Nulls.createNullBitMap;
import static com.teradata.tpcds.Table.SHIP_MODE;
import static com.teradata.tpcds.distribution.ShipModeDistributions.*;
import static com.teradata.tpcds.generator.ShipModeGeneratorColumn.SM_CONTRACT;
import static com.teradata.tpcds.generator.ShipModeGeneratorColumn.SM_NULLS;
import static com.teradata.tpcds.random.RandomValueGenerator.ALPHA_NUMERIC;
import static com.teradata.tpcds.random.RandomValueGenerator.generateRandomCharset;

public class ShipModeRowGenerator
        extends AbstractRowGenerator {
    public ShipModeRowGenerator() {
        super(SHIP_MODE);
    }

    @Override
    public RowGeneratorResult generateRowAndChildRows(long rowNumber, Session session, RowGenerator parentRowGenerator, RowGenerator childRowGenerator) {
        long nullBitMap = createNullBitMap(SHIP_MODE, getRandomNumberStream(SM_NULLS));
        long smShipModeSk = rowNumber;
        String smShipModeId = makeBusinessKey(rowNumber);

        long index = rowNumber;

        String smType = getShipModeTypeForIndexModSize(rowNumber);
        index /= SHIP_MODE_TYPE_DISTRIBUTION.getSize();

        String smCode = getShipModeCodeForIndexModSize(index);

        String smCarrier = getShipModeCarrierAtIndex((int) (rowNumber) - 1);

        String smContract = generateRandomCharset(ALPHA_NUMERIC, 1, 20, getRandomNumberStream(SM_CONTRACT));

        return new RowGeneratorResult(new ShipModeRow(nullBitMap, smShipModeSk, smShipModeId, smType, smCode, smCarrier, smContract));
    }
}
