package com.carpooling.sv.v1.shared.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.carpooling.sv.v1.usuario.infra.adapter.out.persistence.Jpa.SpringDataUsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final SpringDataUsuarioRepository usuarioRepository;

    public CustomUserDetailsService(SpringDataUsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        String query = correo != null ? correo.trim().toLowerCase() : "";

        return usuarioRepository.findByCorreo(query)
            .map(SecurityUser::new)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con correo: " + correo));
    }
}
