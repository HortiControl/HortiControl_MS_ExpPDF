package com.horticontrol.export_service.infrastructure.persistence;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class ConversorDeIdsDePedido implements AttributeConverter<Set<Long>, String> {

    private static final String SEPARADOR = ",";

    @Override
    public String convertToDatabaseColumn(Set<Long> ids) {

        if (ids == null || ids.isEmpty()) {
            return null;
        }

        return ids.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(SEPARADOR));
    }

    @Override
    public Set<Long> convertToEntityAttribute(String texto) {

        if (texto == null || texto.isBlank()) {
            return Set.of();
        }

        return java.util.Arrays.stream(texto.split(SEPARADOR))
                .map(String::trim)
                .filter(parte -> !parte.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
