package main

import (
	"net"
	"log"
	"encoding/json"
	msg "./ServerMessage"
)

func main() {
	conn, err := net.Dial("tcp", "systemoutchat.serveirc.com:8675")
	if err != nil {
		log.Fatal(err)
	}

	_, err = conn.Write([]byte("GET /connect HTTP/1.0\r\n\r\n"))
	if err != nil {
		log.Fatal(err)
	}

	message := new(msg.ServerMessage)
	message.Status = msg.ActionLogIn
	message.Username = new(string)
	*message.Username = "test_user"
	message.Password = new(string)
	*message.Password = "test_password"

	bytes, err := json.Marshal(message)
	if err != nil {
		log.Fatal(err)
	}
	log.Printf("encoded message: %s", bytes)

	_, err = conn.Write(bytes)
	if err != nil {
		log.Fatal(err)
	}

	log.Printf("Sent %+v to server\n", *message)

	rsp := new(msg.ServerMessage)
	json.NewDecoder(conn).Decode(rsp)

	log.Printf("Received %+v from server\n", *rsp)
}