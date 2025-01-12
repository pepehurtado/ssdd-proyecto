package es.um.sisdist.backend.Service;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Dialogue;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.User;
import es.um.sisdist.backend.dao.models.utils.UserUtils;
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
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.List;

import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.models.PromptDTO;
import es.um.sisdist.models.PromptUtils;

import jakarta.ws.rs.core.HttpHeaders;


@Path("/u")
public class UsersEndpoint {
    private static final Logger logger = Logger.getLogger(UsersEndpoint.class.getName());
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserInfo(@PathParam("username") String username, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
        Optional<User> user = impl.getUserById(username);
        if (!user.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(UserDTOUtils.toDTO(user.get())).build();
        }
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(UserDTO userDTO, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
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
    public Response deleteUser(@PathParam("username") String user, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
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
    public Response createDialogue(@PathParam("username") String user, DialogueDTO dialogueId, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
        if (dialogueId.getDialogueId().contains(" ") || dialogueId.getDialogueId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El dialogo no puede contener espacios ni estar vacio")
                    .build();
        }


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
                    .entity("El dialogo ya existe")
                    .build();
        }

    }

    @GET
    @Path("/{username}/dialogue")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserDialogues(@PathParam("username") String userId, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
        try {
            Optional<User> userOptional = impl.getUserById(userId);
            if (!userOptional.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            User user = userOptional.get();
            logger.info("Usuario: " + user);
            List<Dialogue> dialogues = user.getDialogues();
            logger.info("Dialogo: " + dialogues);

            return Response.ok(dialogues).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error dialogues").build();
        }   
    }

    @PUT
    @Path("/{username}/dialogue/{dialogueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDialogue(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, DialogueDTO dialogueDTO, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
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
    public Response deleteDialogue(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
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

    @GET
    @Path("/{username}/dialogue/{dialogueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDialogue(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }
        try {
            Optional<User> userOptional = impl.getUserById(userId);
            if (!userOptional.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
            }

            User user = userOptional.get();
            List<Dialogue> dialogues = user.getDialogues();
            logger.info("Dialogues: " + dialogues);
            Optional<Dialogue> dialogueOptional = dialogues.stream().filter(d -> d.getDialogueId().equals(dialogueId)).findFirst();
            if (!dialogueOptional.isPresent()) {
                return Response.status(Response.Status.NOT_FOUND).entity("Dialogue not found").build();
            }

            Dialogue dialogue = dialogueOptional.get();
            logger.info("Dialogue: " + dialogue);
            return Response.ok(dialogue).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error getting dialogue").build();
        }
    }

    @POST
    @Path("/{username}/dialogue/{dialogueId}/{nextUrl}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPrompt(@PathParam("username") String userId, @PathParam("dialogueId") String dialogueId, @PathParam("nextUrl") String nextUrl, PromptDTO pdto, @Context UriInfo uriInfo, @Context HttpHeaders headers) {
        if (!authenticateUser(uriInfo, headers)) {
            return Response.status(Response.Status.UNAUTHORIZED).build(); // Retornar respuesta de autenticación si no es null
        }

        try {
            logger.info("PromptDTO: " + pdto);
            Prompt prompt = PromptUtils.fromDTO(pdto);
            logger.info("Prompt: " + prompt);
            String success = impl.addPrompt(userId, dialogueId, nextUrl, prompt);
            if (success.equals("ok")) {
                return Response.status(Response.Status.CREATED)
                .header("Location", "/Service/u/" + userId + "/dialogue/" +
                        dialogueId)
                .entity("{\"status\":\"Prompt sent!\"}")
                .build();
            } else {
                if(success.equals("El dialogo esta ocupado")){
                    return Response.status(Response.Status.NO_CONTENT).entity("El dialogo esta ocupado").build();
                }
                if(success.equals("El dialogo esta finalizado")){
                    return Response.status(Response.Status.CONFLICT).entity("El dialogo esta finalizado").build();
                }
                else{
                    return Response.status(Response.Status.BAD_REQUEST).entity(success).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error adding prompt").build();
        }
    }

    private boolean authenticateUser(UriInfo uriInfo, HttpHeaders headers) {
        Logger logger = Logger.getLogger(UsersEndpoint.class.getName());

        String ServerAux = System.getenv("AUX_SERVER");
        logger.info("ServerAux: " + ServerAux);

        if (ServerAux == null || "false".equals(ServerAux)) {
            return true;
        }

        String user = headers.getHeaderString("User");
        String date = headers.getHeaderString("Date");
        String authToken = headers.getHeaderString("Auth-Token");

        if (user == null || date == null || authToken == null) {
            logger.severe("Faltan cabeceras:\n" + "User: " + user + "\nDate: " + date + "\nAuth-Token: " + authToken);
            return false;
        }

        User u = impl.getUserById(user).orElse(null);
        if (u == null) {
            logger.severe("Usuario no encontrado: " + user);
            return false;
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setLenient(false);
            dateFormat.parse(date);
        } catch (Exception e) {
            logger.severe("Formato fecha invalida");
            return false;
        }

        logger.info("URL: " + uriInfo.getRequestUri() + "\nDATE: " + date + "\nTOKEN: " + u.getToken());

        String expectedToken = UserUtils.md5pass(uriInfo.getRequestUri().toString() + date + u.getToken());

        logger.info("expectedToken: " + expectedToken + " authtoken: " + authToken);
        if (!expectedToken.equals(authToken)) {
            logger.severe("Token Auth invalido del: " + user + ". Esperado: " + expectedToken + ", recibido: " + authToken);
            return false;
        }

        logger.info("User " + user + " autenticado en el servidor REST externo");
        return true;
    }

}
