package com.jcsoft.springbootreactor.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
@Builder
public class Comentarios
{
    @Singular
    private List<String> comentarios = new ArrayList<>();
}
