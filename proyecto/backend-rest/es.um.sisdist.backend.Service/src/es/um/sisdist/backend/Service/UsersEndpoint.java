package es.um.sisdist.backend.Service;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.backend.dao.models.Dialogue;
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
import java.util.stream.Collectors;
import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.List;

import es.um.sisdist.backend.dao.models.Prompt;
import es.um.sisdist.models.PromptDTO;
import es.um.sisdist.models.PromptUtils;

import jakarta.ws.rs.core.HttpHeaders;

import java.util.Date;
import es.um.sisdist.backend.dao.models.User;

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
            boolean success = impl.addPrompt(userId, dialogueId, nextUrl, prompt);
            if (success) {
                return Response.status(Response.Status.CREATED)
                .header("Location", "/Service/u/" + userId + "/dialogue/" +
                        dialogueId)
                .entity("{\"status\":\"Prompt sent!\"}")
                .build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).entity("Dialogue not found").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error adding prompt").build();
        }
    }

    /*private Response authenticateUser(UriInfo uriInfo, HttpHeaders headers) {
        String isAuxServer = System.getenv("AUX_SERVER");
        Logger.getLogger(UsersEndpoint.class.getName()).info("Is auxiliary server: " + isAuxServer);

        // Si no es servidor auxiliar, no es necesaria la autenticación
        if (isAuxServer == null || isAuxServer.equals("false")) {
            return null; // Autenticación no requerida
        }

    // Obtenemos los valores de las cabeceras
        String user = headers.getHeaderString("Username");
        String date = headers.getHeaderString("Request-Date");
        String authToken = headers.getHeaderString("Auth-Token");

        // Comprobamos que los valores no sean nulos
        if (user == null || date == null || authToken == null) {
            Logger.getLogger(this.getClass().getName()).severe("Missing headers:\n" + "Username: " + user + "\nRequest-Date: " + date + "\nAuth-Token: " + authToken);
            return Response.status(Response.Status.BAD_REQUEST).entity("Missing headers").build();
        }   

        // Comprobamos que el usuario existe
        User u = impl.getUserById(user).orElse(null);
        if (u == null) {
            Logger.getLogger(UsersEndpoint.class.getName()).severe("User not found: " + user);
            return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
        }

        // Verificamos el formato de la fecha
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            dateFormat.setLenient(false);
            Date parsedDate = dateFormat.parse(date);
        } catch (Exception e) {
            Logger.getLogger(UsersEndpoint.class.getName()).severe("Invalid date format");
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid date format").build();
        }

        // Loggeamos la URL, DATE y TOKEN del usuario
        Logger.getLogger(UsersEndpoint.class.getName()).info("\nURL: " + uriInfo.getRequestUri() + "\nRequest-Date: " + date + "\nTOKEN: " + u.getToken());

        // Generamos el token esperado
       // String expectedToken;
       // try {
        String token = UserUtils.md5pass(uriInfo.getRequestUri().toString() + date + u.getToken());
           /* MessageDigest md = MessageDigest.getInstance("MD5");
            String tokenInput = uriInfo.getRequestUri().toString() + date + u.getToken();
            byte[] digest = md.digest(tokenInput.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            expectedToken = sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            Logger.getLogger(UsersEndpoint.class.getName()).severe("Error generating token");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error generating token").build();
        }*/

    /*    // Comprobamos que el token sea correcto
        if (!token.equals(authToken)) {
            Logger.getLogger(UsersEndpoint.class.getName()).severe("Invalid Auth token for `" + user + "`. Expected: " + token + ", received: " + authToken);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Auth token").build();
        }

        // Loggeamos la autenticación
        Logger.getLogger(UsersEndpoint.class.getName()).info("User `" + user + "` authenticated in external REST server");

        return null; // Autenticación exitosa
}*/

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
