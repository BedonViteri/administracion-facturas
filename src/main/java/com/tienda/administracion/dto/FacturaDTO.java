package com.tienda.administracion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FacturaDTO {
    private Long id;
    private String fecha;
    private Double total;
    private String estado;
    private ClienteDTO cliente;
    private List<DetalleDTO> detalles;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClienteDTO {
        private Long id;
        private String cedula;
        private String nombre;
        private String apellido;
        private String correo;
        private String direccion;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DetalleDTO {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private Integer cantidad;
        private Double precioUnitario;
        private Double subtotal;
    }
}