package es.um.sisdist.backend.Service;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.DialogueDTO;
import es.um.sisdist.models.UserDTOUtils;
import es.um.sisdist.models.DialogueUtils;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/u")
public class UsersEndpoint {
    private static final Logger logger = Logger.getLogger(UsersEndpoint.class.getName());
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username) {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDTO userDTO) {
        // Convertir UserDTO a User
        User user = UserDTOUtils.fromDTO(userDTO);
        Logger.getLogger(UsersEndpoint.class.getName()).info("Register user: " + user.toString());
        // Guardar el nuevo usuario usando la lógica de la aplicación
        boolean success = impl.addUser(user);
        System.out.println("Metodo addUser: " + success);
        if (success) {
            logger.info("Usuario creado exitosamente: " + user);
            return Response.status(Response.Status.CREATED).entity(UserDTOUtils.toDTO(user)).build();
        } else {
            logger.warning("No se pudo crear el usuario: " + user);
            return Response.status(Response.Status.BAD_REQUEST).entity("User " + user.toString() + "error").build();
        }
    }

    @POST
    @Path("/{username}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDialogue(@PathParam("user") String user, DialogueDTO dialogueId){
        Dialogue dialogue = DialogueUtils.fromDTO(dialogueId);
        boolean success = impl.createDialogue(user, dialogue);
        if (success) {
            return Response.status(Response.Status.CREATED)
                    .header("Location", "/Service/u/" + user + "/dialogue/" +dialogue.getDialogueId())
                    .build();
        } else {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"Error\":\"Ya existe un nombre para ese dialogo\"}")
                    .build();
        }

    }
}
