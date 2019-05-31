/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.avbravo.crud.security;

import com.avbravo.jmoordb.configuration.JmoordbContext;
import com.avbravo.jmoordbutils.JsfUtil;

import com.avbravo.jmoordbutils.JmoordbResourcesFiles;
import com.crudejb.entity.Rol;
import com.crudejb.entity.Usuario;
import com.crudejb.repository.UsuarioRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.security.enterprise.credential.Credential;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.security.enterprise.identitystore.CredentialValidationResult;
import javax.security.enterprise.identitystore.IdentityStore;

/**
 *
 * @author avbravo
 */
@ApplicationScoped
public class CustomInMemoryIdentityStore implements IdentityStore {

    private String rolValue = "";
    @Inject
    JmoordbResourcesFiles rf;
    @Inject
    UsuarioRepository usuarioRepository;

    @Override
    public CredentialValidationResult validate(Credential credential) {
        UsernamePasswordCredential login = (UsernamePasswordCredential) credential;
        //Esta adaptado para guardar el GRUPO DEL USUARIO la validacion se hizo en LoginController
        String username = login.getCaller();
        String password = login.getPasswordAsString();

        if (!isValidUser(username, password)) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
        return new CredentialValidationResult(rolValue, new HashSet<>(Arrays.asList(rolValue)));

    }

    // <editor-fold defaultstate="collapsed" desc="Boolean isValidUser(String username, String password) ">
    private Boolean isValidUser(String username, String password) {
        try {

            if (!isValidData(username, password)) {
                return false;
            }
            Usuario usuario = new Usuario();
            usuario.setUsername(username);

            Rol rol = (Rol) JmoordbContext.get("jmoordb_rol");
//Asigna el rol del usuario
            this.rolValue = rol.getIdrol();

            //-----------------
            usuario.setUsername(username);

            Optional<Usuario> optional = usuarioRepository.findById(usuario);
            if (!optional.isPresent()) {
                JsfUtil.warningMessage(rf.getAppMessage("login.usernamenotvalid"));
                return false;
            } else {
                Usuario u2 = optional.get();

                usuario = u2;
                //guarda el usuario logeado
                JmoordbContext.put("jmoordb_user", usuario);

                if (!JsfUtil.desencriptar(usuario.getPassword()).equals(password)) {
                    JsfUtil.successMessage(rf.getAppMessage("login.passwordnotvalid"));
                    return false;
                }
                if (usuario.getActivo().equals("no")) {
                    JsfUtil.successMessage(rf.getAppMessage("login.usuarioinactivo"));
                    return false;
                }

            

                //Valida los roles del usuario si coincide con el seleccionado
                Boolean foundrol = false;
                for (Rol r : usuario.getRol()) {
                    if (rol.getIdrol().equals(r.getIdrol())) {
                        foundrol = true;
                    }
                }
                if (!foundrol) {
                    JsfUtil.successMessage(rf.getAppMessage("login.notienerolenelsistema") + " " + rol.getIdrol());
                    return false;
                }
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Boolean isValidData(String username, String password)">
    private Boolean isValidData(String username, String password) {
        try {
            if (username.isEmpty() || username.equals("") || username == null) {
                JsfUtil.successMessage(rf.getAppMessage("warning.usernameisempty"));
                return false;
            }
            if (password.isEmpty() || password.equals("") || password == null) {
                JsfUtil.successMessage(rf.getAppMessage("warning.passwordisempty"));
                return false;
            }
            return true;
        } catch (Exception e) {
        }

        return false;
    }
    // </editor-fold>
}
