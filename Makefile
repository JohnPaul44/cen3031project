build_server:
	go build MessageHandlers.go Server.go

run_server:
	go run MessageHandlers.go Server.go

run_client:
	go run Errors.go ServerMessage.go Test_Client.go

