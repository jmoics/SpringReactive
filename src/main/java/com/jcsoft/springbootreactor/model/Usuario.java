package com.jcsoft.springbootreactor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class Usuario
{
    private String nombre;
    private String apellido;
}
