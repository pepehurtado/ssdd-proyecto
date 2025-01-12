package es.um.sisdist.backend.grpc.impl;

import java.time.LocalDateTime;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import io.grpc.stub.StreamObserver;
import es.um.sisdist.backend.dao.user.IUserDAO;
import es.um.sisdist.backend.dao.models.DialogueEstados;
import es.um.sisdist.backend.dao.models.Prompt;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Conversaciones extends Thread {
    private PromptRequest request;
    private StreamObserver<PromptResponse> responseObserver;
    private int i;
    private IUserDAO dao;

    public Conversaciones(PromptRequest request, StreamObserver<PromptResponse> responseObserver, int i, IUserDAO dao) {
        super();
        this.request = request;
        this.responseObserver = responseObserver;
        this.i = i;
        this.dao = dao;
    }

    @Override
    public void run() {
        try {
            LocalDateTime timestamp = LocalDateTime.parse(request.getTimestamp());
            Prompt promptMensaje = new Prompt(request.getPrompt(), "", timestamp);
            String token = enviarLlamaChat(promptMensaje.getPrompt());
            String respuesta = getLlamaChatResponse(token);
            promptMensaje.setAnswer(respuesta);
            dao.addPromptRespuesta(request.getUserId(), request.getDialogueId(), promptMensaje);
            dao.updateDialogueEstado(request.getUserId(), request.getDialogueId(), DialogueEstados.READY);
            responseObserver.onNext(
                PromptResponse.newBuilder()
                    .setSuccess(true)
                    .build()
            );
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }
    

    private String enviarLlamaChat(String prompt) {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://ssdd-llamachat:5020/prompt"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"prompt\":\"" + prompt + "\"}"))
                .build();

        try {
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            String token = "0";
            List<String> location = httpResponse.headers().allValues("Location");
            if (!location.isEmpty()) {
                token = location.get(0).split("/")[2];
            }
            return token;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "0";
        }
    }

    private String getLlamaChatResponse(String token) {
        int num = 0;
        boolean logrado = false;
        String respuesta = "";
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://ssdd-llamachat:5020/response/" + token))
                .header("Accept", "*/*")
                .build();

        while (num < 100 && !logrado) {
            try {
                HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (httpResponse.statusCode() == 200) {
                    Pattern patron = Pattern.compile("\"answer\": \"(.*)\",");
                    Matcher matcher = patron.matcher(httpResponse.body());
                    if (matcher.find()) {
                        respuesta = matcher.group(1);
                        logrado = true;
                    }
                } else {
                    Thread.sleep(1000);
                }
                num++;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
        return respuesta;
    }
}
