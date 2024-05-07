package com.daimler.data.db.jsonb.userwidgetpref;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WidgetUserInformationOwner implements Serializable{
    private static final long serialVersionUID = -6062902528095302918L;

    private String id;
    private String name;
    
}
