package com.carpooling.sv.v1.viaje.domain.value;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HexFormat;
import java.util.Locale;

public record Coordenada(double latitud, double longitud) {
    public Coordenada {
        if (latitud < -90.0 || latitud > 90.0) {
            throw new IllegalArgumentException("La latitud debe estar en el rango de -90 a 90");
        }
        if (longitud < -180.0 || longitud > 180.0) {
            throw new IllegalArgumentException("La longitud debe estar en el rango de -180 a 180");
        }
    }

    public String toWkt() {
        return String.format(Locale.US, "POINT(%.6f %.6f)", longitud, latitud);
    }

    public static Coordenada fromWkt(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("El formato de coordenadas no puede ser nulo o vacío");
        }
        String clean = input.trim();

        // Soporte para PostGIS EWKB Hexadecimal (ej: 0101000020E6100000...)
        if (clean.startsWith("0101") && clean.length() >= 42) {
            try {
                byte[] bytes = HexFormat.of().parseHex(clean);
                ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
                buffer.get(); // byte order (1)
                int geomType = buffer.getInt(); // tipo con flags
                if ((geomType & 0x20000000) != 0) {
                    buffer.getInt(); // consumir SRID
                }
                double lon = buffer.getDouble();
                double lat = buffer.getDouble();
                return new Coordenada(lat, lon);
            } catch (Exception e) {
                // Si falla el parseo de hex, continúa intentando WKT estándar
            }
        }

        String upper = clean.toUpperCase();
        if (upper.startsWith("POINT") && upper.contains("(") && upper.contains(")")) {
            int start = upper.indexOf('(') + 1;
            int end = upper.indexOf(')');
            String coordsStr = upper.substring(start, end).trim();
            String[] parts = coordsStr.split("\\s+");
            if (parts.length >= 2) {
                double lon = Double.parseDouble(parts[0]);
                double lat = Double.parseDouble(parts[1]);
                return new Coordenada(lat, lon);
            }
        }

        throw new IllegalArgumentException("Formato de geometría no compatible: " + input);
    }
}
