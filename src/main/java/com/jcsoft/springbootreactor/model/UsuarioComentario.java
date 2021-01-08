package com.jcsoft.springbootreactor.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
@ToString
@Builder
public class UsuarioComentario
{
    private Usuario usuario;
    private Comentarios comentarios;
}
