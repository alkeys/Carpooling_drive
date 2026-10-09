package com.carpooling.sv.v1.mensajeria.infra.adapter.in.web;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.carpooling.sv.v1.mensajeria.application.port.in.CrearOObtenerConversacionViajeCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.EnviarMensajeCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.GetConversacionesUsuarioCase;
import com.carpooling.sv.v1.mensajeria.application.port.in.GetMensajesConversacionCase;
import com.carpooling.sv.v1.mensajeria.application.service.MensajeriaService;
import com.carpooling.sv.v1.mensajeria.domain.Conversacion;
import com.carpooling.sv.v1.mensajeria.domain.Mensaje;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.ConversacionAdminResponse;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.ConversacionResponse;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeEnviarRequest;
import com.carpooling.sv.v1.mensajeria.infra.adapter.in.web.dto.MensajeResponse;
import com.carpooling.sv.v1.shared.security.SecurityContextHelper;
import com.carpooling.sv.v1.usuario.application.port.out.UsuarioRepositoryPort;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/conversaciones")
@Tag(name = "Mensajería y Chat", description = "Chat grupal por viaje y mensajería entre conductor, pasajeros y soporte administrativo")
@SecurityRequirement(name = "bearerAuth")
public class MensajeriaController {

    private final CrearOObtenerConversacionViajeCase crearOObtenerConversacionViajeCase;
    private final EnviarMensajeCase enviarMensajeCase;
    private final GetMensajesConversacionCase getMensajesConversacionCase;
    private final GetConversacionesUsuarioCase getConversacionesUsuarioCase;
    private final MensajeriaService mensajeriaService;
    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final SecurityContextHelper securityHelper;

    public MensajeriaController(
            CrearOObtenerConversacionViajeCase crearOObtenerConversacionViajeCase,
            EnviarMensajeCase enviarMensajeCase,
            GetMensajesConversacionCase getMensajesConversacionCase,
            GetConversacionesUsuarioCase getConversacionesUsuarioCase,
            MensajeriaService mensajeriaService,
            UsuarioRepositoryPort usuarioRepositoryPort,
            SecurityContextHelper securityHelper) {
        this.crearOObtenerConversacionViajeCase = crearOObtenerConversacionViajeCase;
        this.enviarMensajeCase = enviarMensajeCase;
        this.getMensajesConversacionCase = getMensajesConversacionCase;
        this.getConversacionesUsuarioCase = getConversacionesUsuarioCase;
        this.mensajeriaService = mensajeriaService;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.securityHelper = securityHelper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las conversaciones (Admin)", description = "Obtiene todas las salas de chat y tickets de viajes para supervisión administrativa.")
    @ApiResponse(responseCode = "200", description = "Lista completa de conversaciones para administración")
    public ResponseEntity<List<ConversacionAdminResponse>> getTodasLasConversaciones() {
        return ResponseEntity.ok(mensajeriaService.getTodasConversacionesAdmin());
    }

    @GetMapping("/viaje/{viajeId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener o abrir chat del viaje", description = "Obtiene o crea la sala de chat del viaje. Solo el conductor y los pasajeros aceptados tienen acceso.")
    @ApiResponse(responseCode = "200", description = "Conversación obtenida exitosamente")
    @ApiResponse(responseCode = "403", description = "No eres participante ni pasajero confirmado del viaje")
    @ApiResponse(responseCode = "404", description = "Viaje no encontrado")
    public ResponseEntity<ConversacionResponse> getConversacionPorViaje(@PathVariable UUID viajeId) {
        Conversacion conversacion = crearOObtenerConversacionViajeCase.obtenerOCrearConversacionPorViaje(viajeId);
        return ResponseEntity.ok(ConversacionResponse.fromDomain(conversacion));
    }

    @GetMapping("/mis-conversaciones")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mis conversaciones", description = "Obtiene todas las salas de chat en las que participa el usuario autenticado.")
    @ApiResponse(responseCode = "200", description = "Lista de conversaciones")
    public ResponseEntity<List<ConversacionResponse>> getMisConversaciones() {
        UUID currentUserId = securityHelper.getCurrentUserId();
        List<Conversacion> conversaciones = getConversacionesUsuarioCase.getConversacionesPorUsuario(currentUserId);
        List<ConversacionResponse> response = conversaciones.stream()
            .map(ConversacionResponse::fromDomain)
            .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/mensajes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver mensajes del chat", description = "Lista todos los mensajes de una conversación en orden cronológico.")
    @ApiResponse(responseCode = "200", description = "Historial de mensajes obtenido")
    @ApiResponse(responseCode = "403", description = "No eres participante de esta conversación")
    @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    public ResponseEntity<List<MensajeResponse>> getMensajes(@PathVariable UUID id) {
        return ResponseEntity.ok(mensajeriaService.getMensajesDetallados(id));
    }

    @PostMapping("/{id}/mensajes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Enviar mensaje al chat", description = "Envía un mensaje en la conversación grupal del viaje.")
    @ApiResponse(responseCode = "201", description = "Mensaje enviado exitosamente")
    @ApiResponse(responseCode = "400", description = "Contenido inválido o vacío")
    @ApiResponse(responseCode = "403", description = "No tienes permiso para enviar mensajes en esta conversación")
    @ApiResponse(responseCode = "404", description = "Conversación no encontrada")
    public ResponseEntity<MensajeResponse> enviarMensaje(
            @PathVariable UUID id,
            @Valid @RequestBody MensajeEnviarRequest request) {
        Mensaje enviado = mensajeriaService.enviarMensaje(id, request.contenido());
        String nombre = "Usuario";
        String rol = "USUARIO";
        if (enviado.remitenteId() != null) {
            var u = usuarioRepositoryPort.findById(enviado.remitenteId());
            if (u.isPresent()) {
                var usuario = u.get();
                nombre = usuario.nombre();
                if (usuario.roles() != null) {
                    if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.ADMIN)) {
                        rol = "ADMIN";
                    } else if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CONDUCTOR)) {
                        rol = "CONDUCTOR";
                    } else if (usuario.roles().contains(com.carpooling.sv.v1.usuario.domain.value.RolUsuario.CLIENTE)) {
                        rol = "CLIENTE";
                    }
                }
            }
        }
        return ResponseEntity.created(URI.create("/api/v1/conversaciones/" + id + "/mensajes/" + enviado.id()))
            .body(MensajeResponse.fromDomain(enviado, nombre, rol));
    }
}
