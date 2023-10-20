package com.daimler.data.db.jsonb;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSolution implements Serializable {

    private static final long serialVersionUID = 5977725021095023669L;

    private String name;
}