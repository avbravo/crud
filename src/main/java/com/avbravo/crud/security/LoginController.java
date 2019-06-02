/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.avbravo.crud.security;

import com.avbravo.jmoordb.configuration.JmoordbConfiguration;
import com.avbravo.jmoordb.configuration.JmoordbConnection;
import com.avbravo.jmoordb.configuration.JmoordbContext;
import com.avbravo.jmoordb.mongodb.history.repository.AccessInfoRepository;
import com.avbravo.jmoordb.mongodb.history.services.AutoincrementableServices;
import com.avbravo.jmoordb.mongodb.history.repository.ConfiguracionRepository;
import com.avbravo.jmoordb.mongodb.history.services.ConfiguracionServices;
import com.avbravo.jmoordb.mongodb.history.services.ErrorInfoServices;
import com.avbravo.jmoordb.mongodb.history.repository.RevisionHistoryRepository;
import com.avbravo.jmoordb.mongodb.history.entity.Configuracion;
import com.avbravo.jmoordb.services.AccessInfoServices;
import com.avbravo.jmoordb.services.RevisionHistoryServices;
import com.avbravo.jmoordbsecurity.SecurityInterface;
import com.avbravo.jmoordbutils.JmoordbLanguages;
import com.avbravo.jmoordbutils.JsfUtil;
import com.avbravo.jmoordbutils.email.ManagerEmail;
import com.avbravo.jmoordbutils.JmoordbResourcesFiles;
import com.crudejb.entity.Rol;
import com.crudejb.entity.Usuario;
import com.crudejb.repository.RolRepository;
import com.crudejb.repository.UsuarioRepository;
import com.crudejb.services.RolServices;
import com.crudejb.services.UsuarioServices;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import javax.security.enterprise.credential.UsernamePasswordCredential;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;

@Named
@SessionScoped
@Getter
@Setter
public class LoginController implements Serializable, SecurityInterface {
// <editor-fold defaultstate="collapsed" desc="fields">

    @Inject
    private SecurityContext securityContext;
    @Inject
    private ExternalContext externalContext;
    @Inject
    private FacesContext facesContext;
//    @Inject
//JmoordbLanguages jmoordbLanguages;

    //Atributos para la interface IController
    @Inject
    RevisionHistoryRepository revisionHistoryRepository;
    @Inject
    RevisionHistoryServices revisionHistoryServices;
    @Inject
    AutoincrementableServices autoincrementableServices;
    @Inject
    ConfiguracionRepository configuracionRepository;

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginController.class.getName());
    private HashMap<String, String> parameters = new HashMap<>();

    private String passwordold;
    private String passwordnew;
    private String passwordnewrepeat;

    Configuracion configuracion = new Configuracion();
    //Acceso
    @Inject
    AccessInfoServices accessInfoServices;
    @Inject
    AccessInfoRepository accessInfoRepository;
    @Inject
    JmoordbResourcesFiles rf;

    Boolean loggedIn = false;
    private String username;
    private String password;
    private String foto;
    private String id;
    private String key;
    String usernameSelected;
    Boolean recoverSession = false;
    Boolean userwasLoged = false;
    Boolean tokenwassend = false;
    String usernameRecover = "";
    String myemail = "@gmail.com";
    String mytoken = "";
    /*
    
     */

    @Inject
    UsuarioRepository usuarioRepository;
    Usuario usuario = new Usuario();
    @Inject
    RolRepository rolRepository;
    Rol rol = new Rol();

    //Services
    @Inject
    RolServices rolServices;
    @Inject
    ErrorInfoServices errorServices;
    @Inject
    UsuarioServices usuarioServices;
    @Inject
    ConfiguracionServices configuracionServices;
    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="getter/setter">

    
    // </editor-fold>

// <editor-fold defaultstate="collapsed" desc="init">
    @PostConstruct
    public void init() {
     //   jmoordbLanguages.spanishAction();
        loggedIn = false;
        recoverSession = false;
        userwasLoged = false;
        tokenwassend = false;
        configuracion = new Configuracion();
        String demo= JsfUtil.encriptar("demo");
        System.out.println("demo "+demo);

        //Configuracion de la base de datos
        JmoordbConnection jmc = new JmoordbConnection.Builder()
                .withSecurity(false)
                .withDatabase("crud")
                .withHost("")
                .withPort(0)
                .withUsername("")
                .withPassword("")
                .build();
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="destroy">
    @PreDestroy
    public void destroy() {
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="constructor">
    public LoginController() {
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="irLogin">
    public String irLogin() {
//        return "/faces/login";
        return "/login";
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="doLogin">
    public String doLogin() {
        try {
            
         List<Usuario> u = usuarioRepository.findAll();
            for(Usuario u1:u){
              System.out.println("---> "+u1.getUsername() + "" +u1.getPassword());
              
          }

            tokenwassend = false;
            userwasLoged = false;
            loggedIn = true;
            usuario = new Usuario();
            if (username == null || password == null) {
                JsfUtil.warningMessage(rf.getAppMessage("login.usernamenotvalid"));
                return null;
            }
            usernameRecover = usernameRecoveryOfSession();
            recoverSession = !usernameRecover.equals("");
            if (recoverSession) {
                invalidateCurrentSession();
                //  RequestContext.getCurrentInstance().execute("PF('sessionDialog').show();");
                JsfUtil.warningMessage(rf.getAppMessage("session.procederacerrar"));
                return "";
            }
            if (recoverSession && usernameRecover.equals(username)) {
            } else {
                if (isUserLogged(username)) {
                    userwasLoged = true;
                    JsfUtil.warningMessage(rf.getAppMessage("login.alreadylogged"));
                    if (destroyByUsername(username)) {

                    }
                    return "";
                }

            }

            if (!isValidSession(username)) {
                return "";
            }

            /**
             * Cargando la configuracion
             */
            configuracion = configuracionServices.generarConfiguracionInicial(username);

            //----------------------------------------------
//Agregar al context
            JmoordbConfiguration jmc = new JmoordbConfiguration.Builder()
                    .withSpanish(true)
                    .withRepositoryRevisionHistory(revisionHistoryRepository)
                    .withRevisionHistoryServices(revisionHistoryServices)
                    .withRevisionSave(true)
                    .withUsername(username)
                    .build();

            JmoordbContext.put("jmoordb_user", usuario);
            JmoordbContext.put("jmoordb_rol", rol);

//---Injectarlo en el Session
            switch (continueAuthentication()) {
                case SEND_CONTINUE:
                    facesContext.responseComplete();
                    break;
                case SEND_FAILURE:
                    facesContext.addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login failed", null));
                    break;
                case SUCCESS:
                    foto = "img/me.jpg";
                    loggedIn = true;
                    usuario = (Usuario) JmoordbContext.get("jmoordb_user");

                    saveUserInSession(username, 2100);
                    accessInfoRepository.save(accessInfoServices.generateAccessInfo(username, "login", rf.getAppMessage("login.welcome")));
                    loggedIn = true;
                    JsfUtil.successMessage(rf.getAppMessage("login.welcome") + " " + usuario.getNombre());

  return "/faces/pages/index.xhtml?faces-redirect=true";
                case NOT_DONE:
            }

            //-----------------------------
            //              return "/dashboard.xhtml?faces-redirect=true";
        } catch (Exception e) {
            errorServices.errorMessage(JsfUtil.nameOfClass(), JsfUtil.nameOfMethod(), e.getLocalizedMessage());
        }
        return "";
    }

    // </editor-fold>
    private AuthenticationStatus continueAuthentication() {
        return securityContext.authenticate(
                (HttpServletRequest) externalContext.getRequest(),
                (HttpServletResponse) externalContext.getResponse(),
                AuthenticationParameters.withParams()
                        .credential(new UsernamePasswordCredential(username, password))
        );
    }

   

    // <editor-fold defaultstate="collapsed" desc="sendToken()"> 
    public String sendToken() {
        try {

//            if(!myemail.equals("emailusuario")){
//                //no es el email del usuario
//            }
            ManagerEmail managerEmail = new ManagerEmail();
            String token = tokenOfUsername(username);
            if (!token.equals("")) {

                String texto = rf.getAppMessage("token.forinitsession") + " " + token + rf.getAppMessage("token.forinvalidate ");
                if (managerEmail.send(myemail, rf.getAppMessage("token.tokenofsecurity"), texto, "adminemail@gmail.com", "adminpasswordemail")) {
                    JsfUtil.successMessage(rf.getAppMessage("token.wassendtoemail"));
                    tokenwassend = true;
                } else {
                    JsfUtil.warningMessage(rf.getAppMessage("token.errortosendemail"));
                }
            } else {
                JsfUtil.warningMessage(rf.getAppMessage("token.asiganedtouser"));
            }

        } catch (Exception e) {
            errorServices.errorMessage(JsfUtil.nameOfClass(), JsfUtil.nameOfMethod(), e.getLocalizedMessage());
        }
        return "";
    }// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="destroyByUser()"> 

    public String destroyByUser() {
        try {

            userwasLoged = !destroyByUsername(username);
            if (!userwasLoged) {
                JsfUtil.successMessage(rf.getAppMessage("session.destroyedloginagain"));
            } else {
                JsfUtil.successMessage(rf.getAppMessage("session.notdestroyed"));
            }

        } catch (Exception e) {
            errorServices.errorMessage(JsfUtil.nameOfClass(), JsfUtil.nameOfMethod(), e.getLocalizedMessage());
        }
        return "";
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="destroyWithToken()">

    public String destroyByToken() {
        try {

            userwasLoged = !destroyByToken(username, mytoken);

        } catch (Exception e) {
            JsfUtil.warningMessage(rf.getAppMessage("warning.usernotvalid"));
        }
        return "";
    }
// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="invalidateCurrentSession"> 

    public String invalidateCurrentSession() {
        try {
            if (invalidateMySession()) {
                JsfUtil.successMessage(rf.getAppMessage("sesion.invalidate"));
            } else {
                JsfUtil.warningMessage(rf.getAppMessage("sesion.errortoinvalidate"));
            }

        } catch (Exception e) {
            JsfUtil.successMessage("invalidateCurrentSession() " + e.getLocalizedMessage());
        }
        return "";
    }// </editor-fold>
// <editor-fold defaultstate="collapsed" desc="doLogout">

    public String doLogout() {
        return logout("/crud/faces/login.xhtml?faces-redirect=true");
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="changePassword">
    public String changePassword() {
        try {
            if (passwordold.isEmpty() || passwordold.equals("") || passwordold == null) {
                //password anterior no debe estar vacio
                JsfUtil.warningMessage(rf.getMessage("warning.passwordvacio"));
                return "";
            }
            if (passwordnew.isEmpty() || passwordnew.equals("") || passwordold == null) {
                //password nuevo no debe estar vacio
                JsfUtil.warningMessage(rf.getMessage("warning.passwordnuevovacio"));
                return "";
            }
            if (passwordnewrepeat.isEmpty() || passwordnewrepeat.equals("") || passwordnewrepeat == null) {
                //el password repetido no coincide
                JsfUtil.warningMessage(rf.getMessage("warning.passwordnuevorepetidovacio"));
                return "";
            }
            if (!passwordnew.equals(passwordnewrepeat)) {
                //password nuevo no coincide
                JsfUtil.warningMessage(rf.getMessage("warning.passwordnocoinciden"));
                return "";
            }

            if (!passwordold.equals(JsfUtil.desencriptar(usuario.getPassword()))) {
                //password anterior no valido
                JsfUtil.warningMessage(rf.getMessage("warning.passwordanteriornoescorrecto"));
                return "";
            }
            if (passwordold.equals(passwordnew)) {
                //esta colocando el password anterior como nuevo
                JsfUtil.warningMessage(rf.getMessage("warning.passwordanteriorigualalnuevo"));
                return "";
            }
            usuario.setPassword(JsfUtil.encriptar(passwordnew));
            usuarioRepository.update(usuario);
            JsfUtil.successMessage(rf.getAppMessage("info.update"));
        } catch (Exception e) {
            errorServices.errorMessage(JsfUtil.nameOfClass(), JsfUtil.nameOfMethod(), e.getLocalizedMessage());
        }
        return null;
    }
    // </editor-fold>

}
