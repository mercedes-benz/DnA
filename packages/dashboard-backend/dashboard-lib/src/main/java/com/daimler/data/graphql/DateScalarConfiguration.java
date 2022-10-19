package com.daimler.data.graphql;

import static graphql.scalars.util.Kit.typeName;

import java.text.SimpleDateFormat;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;

@Configuration
public class DateScalarConfiguration  {

	static SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");
	
    @Bean
    public GraphQLScalarType isoDateScalar() {
        return GraphQLScalarType.newScalar()
            .name("ISODate")
            .description("ISODate as scalar.")
            .coercing(new Coercing<Date, String>() {
            	
                @Override
                public String serialize(final Object input) {
                	String value = "";
                	try {
                	TemporalAccessor temporalAccessor;
                    if (input instanceof TemporalAccessor) {
                        temporalAccessor = (TemporalAccessor) input;
                    } else if (input instanceof String) {
                    	temporalAccessor = (TemporalAccessor) isoFormat.parse(input.toString());
                    } else if (input instanceof Date) {
                    	value = isoFormat.format(input);
                    } else {
                        throw new CoercingSerializeException(
                                "Expected a 'String' or 'java.time.temporal.TemporalAccessor' but was '" + typeName(input) + "'."
                        );
                    }
                    } catch (Exception e) {
                        throw new CoercingSerializeException(
                                "Unable to turn TemporalAccessor into full date because of : '" + e.getMessage() + "'."
                        );
                    }
                	return value;
                }

                @Override
                public Date parseValue(final Object input) {
                    try {
                        if (input instanceof String) {
                            return isoFormat.parse((String) input);
                        } else {
                            throw new CoercingParseValueException("Expected a String");
                        }
                    } catch (Exception e) {
                        throw new CoercingParseValueException(String.format("Not a valid date: '%s'.", input), e
                        );
                    }
                }

                @Override
                public Date parseLiteral(final Object input) {
                    if (input instanceof StringValue) {
                        try {
                            return isoFormat.parse(((StringValue) input).getValue());
                        } catch (Exception e) {
                            throw new CoercingParseLiteralException(e);
                        }
                    } else {
                        throw new CoercingParseLiteralException("Expected a StringValue.");
                    }
                }
            }).build();
    }
}