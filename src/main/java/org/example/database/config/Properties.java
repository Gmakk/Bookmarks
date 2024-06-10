package org.example.database.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Properties implements Serializable {
    private static final long serialVersionUID = 1L;

    private String url;
    private String username;
    private String password;
}
