package com.tienda.administracion.controller;

import com.tienda.administracion.dto.FacturaDTO;
import com.tienda.administracion.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;

    // ── Página principal ─────────────────────────────────────────────────
    @GetMapping("/")
    public String index(Model model,
                        @RequestParam(required = false) String fecha,
                        @RequestParam(required = false) String cedula) {
        List<FacturaDTO> facturas;

        if (fecha != null && !fecha.isEmpty()) {
            facturas = adminService.buscarPorFecha(fecha);
            model.addAttribute("filtro", "Fecha: " + fecha);
        } else if (cedula != null && !cedula.isEmpty()) {
            facturas = adminService.buscarPorCedula(cedula);
            model.addAttribute("filtro", "Cedula: " + cedula);
        } else {
            facturas = adminService.obtenerFacturas();
            model.addAttribute("filtro", "Todas");
        }

        model.addAttribute("facturas", facturas);
        model.addAttribute("total", facturas.size());
        return "index";
    }

    // ── Reenviar factura por email ────────────────────────────────────────
    @PostMapping("/reenviar/{id}")
    @ResponseBody
    public String reenviarFactura(@PathVariable Long id,
                                  @RequestParam String email) {
        try {
            adminService.reenviarFactura(id, email);
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}