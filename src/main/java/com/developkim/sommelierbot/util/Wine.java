package com.developkim.sommelierbot.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Wine {
    private String name;
    private String price;
    private String url;
}
