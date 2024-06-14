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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.List;

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

    @DELETE
    @Path("/{username}")
    public Response deleteUser(@PathParam("username") String user) {
        logger.info("Intentando eliminar usuario: " + user);
        boolean success = impl.deleteUser(user);
        if (success) {
            logger.info("Usuario eliminado exitosamente: " + user);
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            logger.warning("No se pudo eliminar el usuario: " + user);
            return Response.status(Response.Status.NOT_FOUND).entity("User " + user + " no encontrado").build();
        }
    }

    @POST
    @Path("/{username}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDialogue(@PathParam("username") String user, DialogueDTO dialogueId){
        logger.info("Dialogo si crear: " + dialogueId.getDialogueId() + user);
        Dialogue dialogue = DialogueUtils.fromDTO(dialogueId);
        logger.info("Dialogo creado: " + dialogue.toString());
        logger.info(user.toString());
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

    @GET
    @Path("/{username}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDialogues(@PathParam("username") String userId) {
        try {
            Optional<User> userOptional = impl.getUserById(userId);
            if (!userOptional.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            User user = userOptional.get();
            logger.info("Usuario: " + user);
            List<Dialogue> dialogues = user.getDialogues();
            List<DialogueDTO> dialogueDTOs = dialogues.stream().map(DialogueUtils::toDTO).collect(Collectors.toList());
            logger.info("Dialogo: " + dialogueDTOs);

            return Response.ok(dialogueDTOs).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error dialogues").build();
        }   
    }

    @PUT
    @Path("/{username}/dialogue/{dialogueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDialogue(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, DialogueDTO dialogueDTO) {
        try {
            Dialogue dialogue = DialogueUtils.fromDTO(dialogueDTO);
            boolean success = impl.updateDialogue(userId, dialogueId, dialogue);
            if (success) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Dialogue not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error updating dialogue").build();
        }
    }

    @DELETE
    @Path("/{username}/dialogue/{dialogueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteDialogue(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId) {
        try {
            boolean success = impl.deleteDialogue(userId, dialogueId);
            if (success) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Dialogue not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error deleting dialogue").build();
        }
    }



}
