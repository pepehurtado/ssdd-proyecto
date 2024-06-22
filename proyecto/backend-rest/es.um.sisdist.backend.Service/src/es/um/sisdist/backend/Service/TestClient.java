package es.um.sisdist.backend.Service;

import java.net.URI;

import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;
import java.util.Date;

public class TestClient {
    // Constantes
    private static final String USERNAME = "usuario1";
    private static final String BASE_PATH = "Service/u";
    private static final int TIME = 2;

    public static void main(String[] args) {
        System.out.println("TestClient");

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget service = client.target(getBaseURI());

        // Eliminar usuario
        System.out.println("\n\nEliminar el usuario 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME).request(MediaType.APPLICATION_JSON).delete();
                System.out.println("DELETE " + BASE_PATH + "/" + USERNAME + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
        }

        // Crear usuario
        System.out.println("\n\nCreación del usuario 'usuario1'");
        {
                System.out.println("POST " + BASE_PATH + "/register/" + ": " +  "{\"id\": \"usuario1\", \"name\": \"usuario1\", \"password_hash\": \"usuario1\", \"email\": \"usuario1@gmail.es\"}");
                Response response = service.path(BASE_PATH + "/register/").request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity( "{\"id\": \"usuario1\", \"name\": \"usuario1\", \"password\": \"usuario1\", \"email\": \"usuario1@gmail.es\"}", MediaType.APPLICATION_JSON));
                System.out.println("POST " + BASE_PATH + "/register/" + ": ["+ response.getStatus()+"] " + response.getHeaders()+" --- "+ response.readEntity(String.class));
        }
        
        //Login
        System.out.println("\n\nLogin de usuario 'usuario1'");
        {
                System.out.println("POST " + "Service/checkLogin/" + ": " + "{\"email\": \"usuario1@gmail.es\", \"password\": \"usuario1\"}");
                Response response = service.path("Service/checkLogin/").request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity("{\"email\": \"usuario1@gmail.es\", \"password\": \"usuario1\"}", MediaType.APPLICATION_JSON));
                System.out.println("POST " + "Service/checkLogin/" + ": ["+response.getStatus()+"] " +response.getHeaders()+" --- "+ response.readEntity(String.class));

        }
        
        // Consulta usuario
        System.out.println("\n\nConsulta el usuario 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME).request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Crear diálogo
        System.out.println("\n\nCreación diálogo 'test'");
        {
                System.out.println("POST " + BASE_PATH + "/" + USERNAME + "/dialogue/" + ": " + "{\"dialogueId\": \"test\"}");
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/").request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity("{\"dialogueId\": \"test\"}", MediaType.APPLICATION_JSON));
                System.out.println("POST " + BASE_PATH + "/" + USERNAME + "/dialogue/" + ": ["+response.getStatus()+"] " +response.getHeaders()+" --- "+ response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta diálogo
        System.out.println("\n\nConsulta 'test'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/test").request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + "/dialogue/test" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Modificar diálogo
        System.out.println("\n\nModificar 'test' a 'dialogoClient'");
        {
                System.out.println("PUT " + BASE_PATH + "/" + USERNAME + "/dialogue/test" + ": " + "{\"dialogueId\": \"dialogoClient\"}");
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/test").request(MediaType.APPLICATION_JSON)
                        .put(Entity.entity("{\"dialogueId\": \"dialogoClient\"}", MediaType.APPLICATION_JSON));
                System.out.println("PUT " + BASE_PATH + "/" + USERNAME + "/dialogue/test" + ": ["+response.getStatus()+"] " +response.getHeaders()+" --- "+ response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta diálogo
        System.out.println("\n\nConsulta 'dialogoClient'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient").request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Prompt en diálogo
        System.out.println("\n\nPrompt en 'dialogoClient'");
        {
                Response r = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient").request(MediaType.APPLICATION_JSON).get();
                String responseBody = r.readEntity(String.class);
                String nextUrl = responseBody.split(",")[2].split(":")[1].split("/")[3].split("\"")[0];
                String timestamp = getCurrentTimestamp();
                System.out.println("POST " + BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient/"+nextUrl + ": " + "{\"timestamp\": \"" + timestamp + "\", \"prompt\": \"Hola!\"}");
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient/"+nextUrl).request(MediaType.APPLICATION_JSON)
                        .post(Entity.entity("{\"timestamp\": \"" + timestamp + "\", \"prompt\": \"Hola!\"}", MediaType.APPLICATION_JSON));
                System.out.println("POST " + BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient/"+nextUrl + ": ["+response.getStatus()+"] " +response.getHeaders()+" --- "+ response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta diálogo
        System.out.println("\n\nConsulta 'dialogoClient'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient").request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta dialogos usuario
        System.out.println("\n\nConsulta diálogos de 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/").request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + "/dialogue/" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Eliminar diálogo
        System.out.println("\n\nEliminar 'dialogoClient'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient").request(MediaType.APPLICATION_JSON).delete();
                System.out.println("DELETE " + BASE_PATH + "/" + USERNAME + "/dialogue/dialogoClient" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta dialogos usuario
        System.out.println("\n\nConsulta diálogos de 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME + "/dialogue/").request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + "/dialogue/" + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Eliminar usuario
        System.out.println("\n\nEliminar el usuario 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME).request(MediaType.APPLICATION_JSON).delete();
                System.out.println("DELETE " + BASE_PATH + "/" + USERNAME + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }

        // Consulta usuario
        System.out.println("\n\nConsulta el usuario 'usuario1'");
        {
                Response response = service.path(BASE_PATH + "/" + USERNAME).request(MediaType.APPLICATION_JSON).get();
                System.out.println("GET " + BASE_PATH + "/" + USERNAME + ": ["+response.getStatus()+"] " + response.readEntity(String.class));
                sleep(TIME);
        }
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri(
                "http://localhost:8080/").build();
    }

    private static String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date());
    }    

    private static void sleep(int s){
        try{
            Thread.sleep(s*1000);
        } catch (InterruptedException e){
            return;
        }
    }
}