package com.daimler.data.db.jsonb.userwidgetpref;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetUserCarlafunction implements Serializable {
    private static final long serialVersionUID = -6062902528095302918L;

    private String id;
    private String name;
}

