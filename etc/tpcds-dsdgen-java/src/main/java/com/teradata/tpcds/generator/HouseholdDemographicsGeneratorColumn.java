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

package com.teradata.tpcds.generator;

import com.teradata.tpcds.Table;

import static com.teradata.tpcds.Table.HOUSEHOLD_DEMOGRAPHICS;

public enum HouseholdDemographicsGeneratorColumn
        implements GeneratorColumn {
    HD_DEMO_SK(188, 1),
    HD_INCOME_BAND_ID(189, 1),
    HD_BUY_POTENTIAL(190, 1),
    HD_DEP_COUNT(191, 1),
    HD_VEHICLE_COUNT(192, 1),
    HD_NULLS(193, 2);

    private final int globalColumnNumber;
    private final int seedsPerRow;

    HouseholdDemographicsGeneratorColumn(int globalColumnNumber, int seedsPerRow) {
        this.globalColumnNumber = globalColumnNumber;
        this.seedsPerRow = seedsPerRow;
    }

    @Override
    public Table getTable() {
        return HOUSEHOLD_DEMOGRAPHICS;
    }

    @Override
    public int getGlobalColumnNumber() {
        return globalColumnNumber;
    }

    @Override
    public int getSeedsPerRow() {
        return seedsPerRow;
    }
}
