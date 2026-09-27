package com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.value.EstadoUsuariosEnum;
import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.entity.value.RolUsuarioEnum;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "usuarios")
public class JpaUsuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "nombre", nullable = false, length = 100)
	private String nombre;

	@Column(name = "apellido", length = 100)
	private String apellido;

	@Column(name = "correo", nullable = false, unique = true, length = 150)
	private String correo;

	@Column(name = "telefono", unique = true, length = 20)
	private String telefono;

	@Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
	private String passwordHash;

	@Column(name = "foto_url", columnDefinition = "TEXT")
	private String fotoUrl;

	@Column(name = "verificado", nullable = false)
	private boolean verificado;

	@Column(name = "estado", nullable = false)
	@Enumerated(EnumType.STRING)
	@ColumnTransformer(write = "?::estado_usuario")
	private EstadoUsuariosEnum estado;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "rol", nullable = false)
	@Enumerated(EnumType.STRING)
	private Set<RolUsuarioEnum> roles = new HashSet<>();

	@Column(name = "creado_en", nullable = false)
	private OffsetDateTime creadoEn;

	@Column(name = "actualizado_en", nullable = false)
	private OffsetDateTime actualizadoEn;

	public JpaUsuario(UUID id, String nombre, String apellido, String correo, String telefono, String passwordHash,
			String fotoUrl, boolean verificado, EstadoUsuariosEnum estado, Set<RolUsuarioEnum> roles, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
		this.id = id;
		this.nombre = nombre;
		this.apellido = apellido;
		this.correo = correo;
		this.telefono = telefono;
		this.passwordHash = passwordHash;
		this.fotoUrl = fotoUrl;
		this.verificado = verificado;
		this.estado = estado;
		this.roles = roles != null ? roles : new HashSet<>();
		this.creadoEn = creadoEn;
		this.actualizadoEn = actualizadoEn;
	}

	public JpaUsuario(UUID id, String nombre, String apellido, String correo, String telefono, String passwordHash,
			String fotoUrl, boolean verificado, EstadoUsuariosEnum estado, OffsetDateTime creadoEn, OffsetDateTime actualizadoEn) {
		this(id, nombre, apellido, correo, telefono, passwordHash, fotoUrl, verificado, estado, new HashSet<>(), creadoEn, actualizadoEn);
	}
}
