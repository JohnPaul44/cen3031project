server:
	go run Connections.go Datastore.go Errors.go MessageHandlers.go Server.go ServerMessage.go

client:
	go run Errors.go ServerMessage.go Test_Client.go

