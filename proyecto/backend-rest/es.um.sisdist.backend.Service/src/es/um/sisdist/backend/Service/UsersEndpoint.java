package es.um.sisdist.backend.Service;

import es.um.sisdist.backend.Service.impl.AppLogicImpl;
import es.um.sisdist.models.UserDTO;
import es.um.sisdist.models.UserDTOUtils;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import jakarta.ws.rs.core.Response;

@Path("/u")
public class UsersEndpoint
{
    private AppLogicImpl impl = AppLogicImpl.getInstance();

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public UserDTO getUserInfo(@PathParam("username") String username)
    {
        return UserDTOUtils.toDTO(impl.getUserByEmail(username).orElse(null));
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registrarUser(UserDTO userDTO){
        boolean success = impl.registrarUser(UserDTOUtils.fromDTO(userDTO));

        if (success){
            return Response.status(Response.Status.CREATED).entity(userDTO).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


}
