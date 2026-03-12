package com.tienda.administracion.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.tienda.administracion.dto.FacturaDTO;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${microservicio.facturacion.url}")
    private String facturacionUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Obtener todas las facturas ───────────────────────────────────────
    public List<FacturaDTO> obtenerFacturas() {
        FacturaDTO[] facturas = restTemplate.getForObject(
            facturacionUrl + "/api/facturas", FacturaDTO[].class);
        return facturas != null ? Arrays.asList(facturas) : List.of();
    }

    // ── Buscar facturas por fecha ────────────────────────────────────────
    public List<FacturaDTO> buscarPorFecha(String fecha) {
        return obtenerFacturas().stream()
            .filter(f -> f.getFecha() != null && f.getFecha().startsWith(fecha))
            .collect(Collectors.toList());
    }

    // ── Buscar facturas por cedula ───────────────────────────────────────
    public List<FacturaDTO> buscarPorCedula(String cedula) {
        return obtenerFacturas().stream()
            .filter(f -> f.getCliente() != null &&
                         cedula.equals(f.getCliente().getCedula()))
            .collect(Collectors.toList());
    }

    // ── Reenviar factura por email ───────────────────────────────────────
    public void reenviarFactura(Long id, String emailDestino) throws Exception {
        FacturaDTO factura = obtenerFacturas().stream()
            .filter(f -> f.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));

        byte[] pdf = generarPdf(factura);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(emailDestino);
        helper.setSubject("Factura #" + id + " - Tienda PC");
        helper.setText("Adjunto encontrara su factura de compra.");
        helper.addAttachment("Factura_" + id + ".pdf", new ByteArrayResource(pdf));
        mailSender.send(message);
    }

    // ── Generar PDF ──────────────────────────────────────────────────────
    private byte[] generarPdf(FacturaDTO factura) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);

        String nombre = factura.getCliente() != null
            ? factura.getCliente().getNombre() + " " + factura.getCliente().getApellido()
            : "Cliente";

        doc.add(new Paragraph("TIENDA DE COMPONENTES PC - FACTURA").setBold().setFontSize(18));
        doc.add(new Paragraph("Factura N: " + factura.getId()));
        doc.add(new Paragraph("Cliente: " + nombre));
        doc.add(new Paragraph("Cedula: " + (factura.getCliente() != null ? factura.getCliente().getCedula() : "")));
        doc.add(new Paragraph("Fecha: " + factura.getFecha()));
        doc.add(new Paragraph("\nDetalle:"));

        float[] cols = {80, 280, 90, 90};
        Table table = new Table(cols);
        table.addCell("Cant.");
        table.addCell("Producto");
        table.addCell("Precio U.");
        table.addCell("Subtotal");

        if (factura.getDetalles() != null) {
            for (FacturaDTO.DetalleDTO d : factura.getDetalles()) {
                table.addCell(String.valueOf(d.getCantidad()));
                table.addCell(d.getProductoNombre() != null ? d.getProductoNombre() : "");
                table.addCell("$" + d.getPrecioUnitario());
                table.addCell("$" + d.getSubtotal());
            }
        }

        doc.add(table);
        doc.add(new Paragraph("\nTOTAL: $" + factura.getTotal()).setBold());
        doc.close();
        return out.toByteArray();
    }
}