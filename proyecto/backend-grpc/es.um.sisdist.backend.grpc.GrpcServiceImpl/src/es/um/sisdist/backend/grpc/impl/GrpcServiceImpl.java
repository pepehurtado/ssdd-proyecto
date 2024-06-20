package es.um.sisdist.backend.grpc.impl;

import java.util.logging.Logger;

import es.um.sisdist.backend.grpc.GrpcServiceGrpc;
import es.um.sisdist.backend.grpc.PingRequest;
import es.um.sisdist.backend.grpc.PingResponse;
import es.um.sisdist.backend.grpc.PromptRequest;
import es.um.sisdist.backend.grpc.PromptResponse;
import es.um.HilosConversaciones;
import es.um.sisdist.backend.dao.DAOFactoryImpl;
import es.um.sisdist.backend.dao.IDAOFactory;
import es.um.sisdist.backend.dao.user.IUserDAO;

import java.util.LinkedList;
import java.util.List;

import io.grpc.stub.StreamObserver;

class GrpcServiceImpl extends GrpcServiceGrpc.GrpcServiceImplBase 
{
    private final Logger logger;
	private int cont;
	private List<HilosConversaciones> hilosChat;
	private IUserDAO dao;
	private IDAOFactory dFactory;

	public GrpcServiceImpl(Logger logger){
		super();
		this.logger = Logger.getLogger(GrpcServiceImpl.class.getName());
		this.cont =  0;
		this.hilosChat = new LinkedList<HilosConversaciones>();
		dFactory = new DAOFactoryImpl();
		dao = dFactory.createMongoUserDAO();
	}
	
	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) 
	{
		logger.info("Recived PING request, value = " + request.getV());
		responseObserver.onNext(PingResponse.newBuilder().setV(request.getV()).build());
		responseObserver.onCompleted();
	}

	@Override
    public void sendPrompt(PromptRequest request, StreamObserver<PromptResponse> responseObserver) {
		HilosConversaciones hiloChat = new HilosConversaciones(request, responseObserver, cont++, dao);
		hilosChat.add(hiloChat);
		hiloChat.start();
    }

}