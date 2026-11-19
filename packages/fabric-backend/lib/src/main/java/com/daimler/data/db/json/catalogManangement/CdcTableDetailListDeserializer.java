package com.daimler.data.db.json.catalogManangement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class CdcTableDetailListDeserializer extends JsonDeserializer<List<CdcTableDetail>> {

    @Override
    public List<CdcTableDetail> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<CdcTableDetail> result = new ArrayList<>();

        if (p.currentToken() != JsonToken.START_ARRAY) {
            return result;
        }

        while (p.nextToken() != JsonToken.END_ARRAY) {
            if (p.currentToken() == JsonToken.VALUE_STRING) {
                CdcTableDetail detail = new CdcTableDetail();
                result.add(detail);
            } else if (p.currentToken() == JsonToken.START_OBJECT) {
                CdcTableDetail detail = p.readValueAs(CdcTableDetail.class);
                result.add(detail);
            }
        }

        return result;
    }
}
