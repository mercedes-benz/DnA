package com.daimler.data.application.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.openmetadata.client.model.Column;

class OpenMetadataClientTest {

    private final OpenMetadataClient client = new OpenMetadataClient(null);

    @Test
    void buildsColumnsWithExpectedTypesAndLengths() {
        assertColumn("varchar(50)", Column.DataTypeEnum.VARCHAR, 50);
        assertColumn("VARCHAR(MAX)", Column.DataTypeEnum.VARCHAR, Integer.MAX_VALUE);
        assertColumn("nvarchar(max)", Column.DataTypeEnum.VARCHAR, Integer.MAX_VALUE);
        assertColumn("nchar(10)", Column.DataTypeEnum.CHAR, 10);
        assertColumn("varbinary(max)", Column.DataTypeEnum.VARBINARY, Integer.MAX_VALUE);
        assertColumn("VARCHAR", Column.DataTypeEnum.VARCHAR, Integer.MAX_VALUE);
        assertColumn("nvarchar", Column.DataTypeEnum.VARCHAR, Integer.MAX_VALUE);
        assertColumn("binary", Column.DataTypeEnum.BINARY, Integer.MAX_VALUE);
        assertColumn("char", Column.DataTypeEnum.CHAR, Integer.MAX_VALUE);
        assertColumn("int", Column.DataTypeEnum.INT, null);
        assertColumn("varchar(abc)", Column.DataTypeEnum.VARCHAR, Integer.MAX_VALUE);
    }

    private void assertColumn(String type, Column.DataTypeEnum expectedType, Integer expectedLength) {
        Column column = client.buildColumn("column", null, type, null);

        assertEquals(expectedType, column.getDataType(), type);
        if (expectedLength == null) {
            assertNull(column.getDataLength(), type);
        } else {
            assertEquals(expectedLength, column.getDataLength(), type);
        }
    }
}
